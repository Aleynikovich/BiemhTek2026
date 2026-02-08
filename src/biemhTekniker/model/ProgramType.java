package biemhTekniker.model;

/**
 * Enum representing the type of robot program.
 * Java 7 compatible.
 */
public enum ProgramType {
    /**
     * Robot programs that perform physical movements and operations.
     */
    ROBOT,
    
    /**
     * Vision programs that interact with the camera system without robot motion.
     */
    VISION
}
