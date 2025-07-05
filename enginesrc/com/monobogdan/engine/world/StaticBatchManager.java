package com.monobogdan.engine.world;

import com.monobogdan.engine.*;
import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.GameObject;
import com.monobogdan.engine.world.components.BatchedMeshRenderer;
import com.monobogdan.engine.world.components.MeshRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StaticBatchManager {
    private static final int APPROXIMATE_MATERIALS = 18;
    private static final int APPROXIMATE_RENDERERS = 256;
    private static final int APPROXIMATE_VERTEX_COUNT = 512;

    private static ExecutorService batcherThreadPool = Executors.newFixedThreadPool(1);
    private Future<?> batchBakeTask;

    private class Batch {
        private Mesh mesh;

        private ByteBuffer vertexBuffer, indexBuffer;
        private BaseMesh.TriangleList triList = new BaseMesh.TriangleList();

        public Batch(Material mat) {
            mesh = new Mesh(world.Runtime,"StaticBatch#" + mat.Name);
            mesh.TriangleLists.put("Primary", triList);
            mesh.LinearList.add(triList);
        }

        private ByteBuffer reallocBuffer(ByteBuffer oldBuf, int desiredSize) {
            ByteBuffer ret = ByteBuffer.allocateDirect(desiredSize);
            if(oldBuf != null)
                ret.put((ByteBuffer)oldBuf.position(0));

            return ret;
        }

        public boolean addMesh(BatchedMeshRenderer renderer) {
            BaseMesh.VertexBuffer buf = renderer.Mesh.Buffers[0];

            if(vertexBuffer == null || vertexBuffer.position() + (buf.Vertices.length * BaseMesh.Vertex.Size) > vertexBuffer.capacity()) {
                int currPos = vertexBuffer == null ? 0 : vertexBuffer.capacity();

                vertexBuffer = reallocBuffer(vertexBuffer,currPos + (buf.Vertices.length * BaseMesh.Vertex.Size)).order(ByteOrder.nativeOrder()); // Realloc VertexBuffer if needed
            }

            if(indexBuffer == null || indexBuffer.position() + (buf.Indices.length * 2) > indexBuffer.capacity()) {
                int currPos = indexBuffer == null ? 0 : indexBuffer.capacity();

                indexBuffer = reallocBuffer(indexBuffer, currPos + (buf.Indices.length * 2)).order(ByteOrder.nativeOrder()); // Realloc IndexBuffer if needed
            }

            int baseOffset = vertexBuffer.position() / BaseMesh.Vertex.Size;

            for(int i = 0; i < buf.Vertices.length; i++) {
                BaseMesh.Vertex vert = buf.Vertices[i];
                Vector pos = renderer.Parent.Position;

                vertexBuffer.putFloat(pos.X + vert.X).putFloat(pos.Y + vert.Y).putFloat(pos.Z + vert.Z);
                vertexBuffer.putFloat(vert.NX).putFloat(vert.NY).putFloat(vert.NZ);
                vertexBuffer.putFloat(vert.U).putFloat(vert.V);
            }

            for(int i = 0; i < buf.Indices.length; i++)
                indexBuffer.putShort((short)(baseOffset + buf.Indices[i]));

            renderer.IsTakenByBatcher = true;
            return true;
        }

        public void finish() {
            vertexBuffer.position(0);
            indexBuffer.position(0);

            triList.VertexBufferOffset = 0;
            triList.Offset = 0;
            triList.Count = (indexBuffer.capacity() / 2);

            mesh.upload(vertexBuffer, indexBuffer);
        }
    }

    // Less allocations than HashMap per iterator. Used only for rendering.
    private class BatchHolder {
        public Material Material;
        public Batch Batch;

        public BatchHolder(Material mat, Batch batch) {
            Material = mat;
            Batch = batch;
        }
    }

    private World world;

    private final ArrayList<BatchedMeshRenderer> batchRenderers;
    private HashMap<Material, Batch> meshes;
    private ArrayList<BatchHolder> batchList;

    public StaticBatchManager(World world) {
        final int INSTANCE_POOL_SIZE = 128; // Approx 128 materials for any level

        this.world = world;

        batchRenderers = new ArrayList<BatchedMeshRenderer>(APPROXIMATE_RENDERERS);
        meshes = new HashMap<Material, Batch>(APPROXIMATE_MATERIALS);
        batchList = new ArrayList<BatchHolder>(meshes.size());
    }

    public void bake() {
        int uniqueMaterials = 0;

        batchList.clear();
        batchRenderers.clear();
        world.findComponentsOfType(BatchedMeshRenderer.class, batchRenderers);

        for(int i = 0; i < batchRenderers.size(); i++) {
            BatchedMeshRenderer renderer = batchRenderers.get(i);
            renderer.IsTakenByBatcher = false;

            if(renderer.Mesh != null && renderer.Material != null) {
                if(renderer.Mesh.Buffers.length != 1)
                    continue; // Only simple meshes is supported now

                Batch batch = meshes.get(renderer.Material);

                if(batch == null)
                    meshes.put(renderer.Material, batch = new Batch(renderer.Material));

                batch.addMesh(renderer);
            }
        }

        for(Map.Entry<Material, Batch> materialBatch : meshes.entrySet()) {
            batchList.add(new BatchHolder(materialBatch.getKey(), materialBatch.getValue()));
            materialBatch.getValue().finish(); // Upload mesh to GPU
        }
    }

    public void bakeAsync() {
        if(batchBakeTask != null)
            return;

        batchRenderers.clear();
        world.findComponentsOfType(BatchedMeshRenderer.class, batchRenderers);

        batchBakeTask = batcherThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                int uniqueMaterials = 0;

                batchList.clear();

                synchronized (batchRenderers) {
                    for(int i = 0; i < batchRenderers.size(); i++) {
                        BatchedMeshRenderer renderer = batchRenderers.get(i);
                        renderer.IsTakenByBatcher = false;

                        if(renderer.Mesh != null && renderer.Material != null) {
                            if(renderer.Mesh.Buffers.length != 1)
                                continue; // Only simple meshes is supported now

                            Batch batch = meshes.get(renderer.Material);

                            if(batch == null)
                                meshes.put(renderer.Material, batch = new Batch(renderer.Material));

                            batch.addMesh(renderer);
                        }
                    }
                }


                for(Map.Entry<Material, Batch> materialBatch : meshes.entrySet()) {
                    batchList.add(new BatchHolder(materialBatch.getKey(), materialBatch.getValue()));
                    materialBatch.getValue().finish(); // Upload mesh to GPU
                }
            }
        });
    }

    void draw(Graphics graphics, Camera camera) {
        for(int i = 0; i < batchList.size(); i++) {
            BatchHolder holder = batchList.get(i);

            graphics.drawMesh(holder.Batch.mesh, holder.Material, Matrix.Identity, camera);
        }
    }
}
