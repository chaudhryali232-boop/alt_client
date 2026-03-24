// ... other imports ...
import me.alpha432.oyvey.features.modules.render.BlockHighlightModule;
import me.alpha432.oyvey.features.modules.render.Nametags; // ADD THIS IMPORT

public class ModuleManager implements Jsonable, Util {
    // ... rest of the code ...
    public void init() {
        // ... other registrations ...
        register(new BlockHighlightModule());
        register(new NoFallModule());
        register(new KeyPearlModule());
        register(new Nametags()); // This will now work!
        
        LOGGER.info("Registered {} modules", modules.size());
        // ... rest of the code ...
    }
}
