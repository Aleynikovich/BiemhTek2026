package biemhTekniker.config;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;

/**
 * Centralized repository for station frames.
 * Provides a single point of access to all frames used in the application,
 * separating frame paths from business logic.
 * This makes the code more maintainable and easier to adapt to different station layouts.
 */
public class FrameRepository
{
    private static final Logger log = Logger.getLogger(FrameRepository.class);
    private final RoboticsAPIApplication application;

    public FrameRepository(RoboticsAPIApplication application)
    {
        this.application = application;
    }

    /**
     * Gets a frame by its path in the station setup.
     *
     * @param framePath Full path to the frame (e.g., "/SchunkBase/PickPlaceA")
     * @return ObjectFrame from station setup
     * @throws IllegalArgumentException if frame doesn't exist
     */
    public ObjectFrame getFrame(String framePath)
    {
        try
        {
            ObjectFrame frame = application.getApplicationData().getFrame(framePath);
            if (frame == null)
            {
                throw new IllegalArgumentException("Frame not found: " + framePath);
            }
            return frame;
        } catch (Exception e)
        {
            log.error("Failed to get frame: " + framePath, e);
            throw new IllegalArgumentException("Failed to get frame: " + framePath, e);
        }
    }

    /**
     * Gets a frame copy with redundancy.
     *
     * @param framePath Full path to the frame
     * @return Frame copy with redundancy
     */
    public Frame getFrameCopy(String framePath)
    {
        return getFrame(framePath).copyWithRedundancy();
    }

    /**
     * Gets a frame copy with a Z-axis offset applied.
     *
     * @param framePath Full path to the frame
     * @param zOffset Z offset in millimeters (positive = up, negative = down)
     * @return Frame copy with offset applied
     */
    public Frame getFrameWithOffset(String framePath, double zOffset)
    {
        Frame frame = getFrameCopy(framePath);
        frame.setZ(frame.getZ() + zOffset);
        return frame;
    }

    // ===== Predefined Frame Paths for BiemhTek2026 Project =====
    // These methods provide semantic names for project-specific frames
    // Making it easy to change frame paths without modifying business logic

    /**
     * Gets the scan workpiece position frame.
     *
     * @return Scan workpiece frame
     */
    public ObjectFrame getScanWorkpieceFrame()
    {
        return getFrame("/ScanWorkpiece");
    }

    /**
     * Gets the SchunkBase exit frame.
     *
     * @return Exit frame
     */
    public ObjectFrame getExitFrame()
    {
        return getFrame("/SchunkBase/Exit");
    }

    /**
     * Gets a pick/place frame for a specific gripper.
     *
     * @param gripperLabel Gripper label ("A" or "B")
     * @return Pick/place frame for the specified gripper
     */
    public ObjectFrame getPickPlaceFrame(String gripperLabel)
    {
        if ("A".equals(gripperLabel))
        {
            return getPickPlaceFrameA();
        } else if ("B".equals(gripperLabel))
        {
            return getPickPlaceFrameB();
        } else
        {
            throw new IllegalArgumentException("Invalid gripper label: " + gripperLabel + " (expected A or B)");
        }
    }

    /**
     * Gets the SchunkBase PickPlaceA frame.
     *
     * @return PickPlaceA frame
     */
    public ObjectFrame getPickPlaceFrameA()
    {
        return getFrame("/SchunkBase/PickPlaceA");
    }

    /**
     * Gets the SchunkBase PickPlaceB frame.
     *
     * @return PickPlaceB frame
     */
    public ObjectFrame getPickPlaceFrameB()
    {
        return getFrame("/SchunkBase/PickPlaceB");
    }
}
