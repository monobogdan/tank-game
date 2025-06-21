import com.monobogdan.engine.world.ExportedObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptExports implements ExportedObjects {
    public static Class[] classList = {

    };

    public List<Class> getExportedObjectsClasses() {
        ArrayList<Class> classes = new ArrayList<Class>();

        for(Class _class : classList)
            classes.add(_class);

        return classes;
    }
}
