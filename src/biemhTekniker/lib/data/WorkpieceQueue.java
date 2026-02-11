package biemhTekniker.lib.data;

import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe queue for managing multiple workpieces.
 * Supports the pick-measure-return lifecycle.
 */
public class WorkpieceQueue
{
    private static final Logger log = Logger.getLogger(WorkpieceQueue.class);
    
    /**
     * Position tolerance for workpiece matching in millimeters.
     * Configurable via application.properties (workpiece.position.tolerance.mm).
     * Default is 5mm based on vision system accuracy and mechanical repeatability.
     * Workpieces within this tolerance are considered to be at the same position.
     */
    private final double positionToleranceMm;
    
    private final List<WorkpieceData> workpieces = new ArrayList<WorkpieceData>();

    /**
     * Default constructor using configuration from application.properties.
     */
    public WorkpieceQueue()
    {
        ConfigManager config = ConfigManager.getInstance();
        this.positionToleranceMm = config.getDouble("workpiece.position.tolerance.mm", 5.0);
        log.debug("WorkpieceQueue initialized with position tolerance: " + positionToleranceMm + "mm");
    }

    /**
     * Adds a workpiece to the queue with AVAILABLE state.
     *
     * @param wp Workpiece to add
     */
    public synchronized void addWorkpiece(WorkpieceData wp)
    {
        workpieces.add(wp);
        log.debug("Added workpiece to queue: id=" + wp.getId() + ", ref=" + wp.getReferenceIndex() + ", score=" + wp.getScore());
    }

    /**
     * Returns the highest-score AVAILABLE workpiece and marks it as PICKED.
     * Returns null if no AVAILABLE workpieces exist.
     *
     * @return The workpiece to pick, or null
     */
    public synchronized WorkpieceData takeNextForPicking()
    {
        WorkpieceData best = null;
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            if (wp.getState() == WorkpieceState.AVAILABLE)
            {
                if (best == null || wp.getScore() > best.getScore())
                {
                    best = wp;
                }
            }
        }

        if (best != null)
        {
            best.setState(WorkpieceState.PICKED);
            log.info("Selected workpiece for picking: id=" + best.getId() + ", ref=" + best.getReferenceIndex() + ", score=" + best.getScore());
        } else
        {
            log.debug("No AVAILABLE workpieces to pick");
        }
        return best;
    }

    /**
     * Returns the highest-score AVAILABLE workpiece WITHOUT marking it as PICKED.
     * Returns null if no AVAILABLE workpieces exist.
     * Use this to preview the next workpiece before attempting to pick it.
     *
     * @return The workpiece that would be picked next, or null
     */
    public synchronized WorkpieceData peekNextForPicking()
    {
        WorkpieceData best = null;
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            if (wp.getState() == WorkpieceState.AVAILABLE)
            {
                if (best == null || wp.getScore() > best.getScore())
                {
                    best = wp;
                }
            }
        }

        if (best != null)
        {
            log.debug("Peeked at next workpiece: id=" + best.getId() + ", ref=" + best.getReferenceIndex() + ", score=" + best.getScore());
        } else
        {
            log.debug("No AVAILABLE workpieces to peek");
        }
        return best;
    }

    /**
     * Marks a specific workpiece as PICKED.
     * Used after successfully picking a workpiece that was previewed with peekNextForPicking().
     *
     * @param workpieceId Workpiece ID to mark as picked
     */
    public synchronized void markPicked(long workpieceId)
    {
        WorkpieceData wp = findById(workpieceId);
        if (wp != null)
        {
            wp.setState(WorkpieceState.PICKED);
            log.info("Marked workpiece as PICKED: id=" + workpieceId);
        } else
        {
            log.warn("Cannot mark PICKED - workpiece not found: id=" + workpieceId);
        }
    }

    /**
     * Returns the first workpiece found in the PICKED state.
     * * @return The picked workpiece, or null if none are currently picked.
     */
    public synchronized WorkpieceData getPickedWorkpiece()
    {
        for (WorkpieceData wp : workpieces)
        {
            if (wp.getState() == WorkpieceState.PICKED)
            {
                return wp;
            }
        }
        return null;
    }

    /**
     * Marks a workpiece as MEASURING.
     *
     * @param workpieceId Workpiece ID
     */
    public synchronized void markMeasuring(long workpieceId)
    {
        WorkpieceData wp = findById(workpieceId);
        if (wp != null)
        {
            wp.setState(WorkpieceState.MEASURING);
            log.info("Marked workpiece as MEASURING: id=" + workpieceId);
        } else
        {
            log.warn("Cannot mark MEASURING - workpiece not found: id=" + workpieceId);
        }
    }

    /**
     * Marks a workpiece as MEASURED.
     *
     * @param workpieceId Workpiece ID
     */
    public synchronized void markMeasured(long workpieceId)
    {
        WorkpieceData wp = findById(workpieceId);
        if (wp != null)
        {
            wp.setState(WorkpieceState.MEASURED);
            log.info("Marked workpiece as MEASURED: id=" + workpieceId);
        } else
        {
            log.warn("Cannot mark MEASURED - workpiece not found: id=" + workpieceId);
        }
    }

    /**
     * Returns a MEASURED workpiece for removal from the machine.
     * Sets state to PICKED for transport back to bin.
     * Returns null if no MEASURED workpieces exist.
     *
     * @return The measured workpiece to remove, or null
     */
    public synchronized WorkpieceData takeMeasuredWorkpiece()
    {
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            if (wp.getState() == WorkpieceState.MEASURED)
            {
                wp.setState(WorkpieceState.PICKED);
                log.info("Selected MEASURED workpiece for removal: id=" + wp.getId());
                return wp;
            }
        }
        log.debug("No MEASURED workpieces to remove");
        return null;
    }

    /**
     * Marks a workpiece as RETURNED.
     *
     * @param workpieceId Workpiece ID
     */
    public synchronized void markReturned(long workpieceId)
    {
        WorkpieceData wp = findById(workpieceId);
        if (wp != null)
        {
            wp.setState(WorkpieceState.RETURNED);
            log.info("Marked workpiece as RETURNED: id=" + workpieceId);
        } else
        {
            log.warn("Cannot mark RETURNED - workpiece not found: id=" + workpieceId);
        }
    }

    /**
     * Gets count of AVAILABLE workpieces.
     *
     * @return Number of available workpieces
     */
    public synchronized int getAvailableCount()
    {
        int count = 0;
        for (int i = 0; i < workpieces.size(); i++)
        {
            if (workpieces.get(i).getState() == WorkpieceState.AVAILABLE)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets total workpiece count.
     *
     * @return Total number of workpieces in queue
     */
    public synchronized int getTotalCount()
    {
        return workpieces.size();
    }

    /**
     * Clears all workpieces from the queue.
     */
    public synchronized void clear()
    {
        workpieces.clear();
        log.info("Cleared workpiece queue");
    }

    /**
     * Removes a specific workpiece from the queue by ID.
     * 
     * @param workpieceId ID of the workpiece to remove
     * @return true if workpiece was found and removed, false otherwise
     */
    public synchronized boolean removeWorkpiece(long workpieceId)
    {
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            if (wp.getId() == workpieceId)
            {
                workpieces.remove(i);
                log.info("Removed workpiece from queue: id=" + workpieceId);
                return true;
            }
        }
        log.warn("Workpiece not found in queue: id=" + workpieceId);
        return false;
    }

    /**
     * Returns a formatted status string showing all workpieces and their states.
     *
     * @return Queue status string
     */
    public synchronized String getQueueStatus()
    {
        if (workpieces.isEmpty())
        {
            return "Queue empty";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("Queue status (").append(workpieces.size()).append(" total):\n");
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            sb.append("  [").append(i).append("] ID:").append(wp.getId()).append(" Ref:").append(wp.getReferenceIndex()).append(" State:").append(wp.getState()).append(" Score:").append(String.format("%.2f", wp.getScore())).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Returns workpiece data as JSON array string.
     * Format: [{"id":1,"reference":1,"state":"AVAILABLE","gripper":"A","x":100.0,"y":200.0,"z":50.0,"score":0.95},...]
     *
     * @return JSON array string of workpiece data
     */
    public synchronized String getWorkpiecesJson()
    {
        if (workpieces.isEmpty())
        {
            return "[]";
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < workpieces.size(); i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            WorkpieceData wp = workpieces.get(i);
            sb.append("{");
            sb.append("\"id\":").append(wp.getId());
            sb.append(",\"reference\":").append(wp.getReferenceIndex());
            sb.append(",\"orientation\":").append(wp.getOrientation());
            sb.append(",\"referenceString\":\"").append(wp.getReferenceString()).append("\"");
            sb.append(",\"state\":\"").append(wp.getState()).append("\"");
            
            String gripperLoc = wp.getGripperLocation();
            if (gripperLoc != null)
            {
                sb.append(",\"gripper\":\"").append(gripperLoc).append("\"");
            } else
            {
                sb.append(",\"gripper\":null");
            }
            
            sb.append(",\"x\":").append(wp.getX());
            sb.append(",\"y\":").append(wp.getY());
            sb.append(",\"z\":").append(wp.getZ());
            sb.append(",\"rx\":").append(wp.getRx());
            sb.append(",\"ry\":").append(wp.getRy());
            sb.append(",\"rz\":").append(wp.getRz());
            sb.append(",\"score\":").append(wp.getScore());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Finds a workpiece by ID.
     *
     * @param workpieceId Workpiece ID to find
     * @return WorkpieceData if found, null otherwise
     */
    private WorkpieceData findById(long workpieceId)
    {
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            if (wp.getId() == workpieceId)
            {
                return wp;
            }
        }
        return null;
    }

    /**
     * Public accessor to get a workpiece by ID without changing its state.
     *
     * @param workpieceId the ID to search
     * @return WorkpieceData or null if not found
     */
    public synchronized WorkpieceData getById(long workpieceId)
    {
        return findById(workpieceId);
    }

    /**
     * Finds an existing workpiece at the given position (within configured tolerance).
     * Used for tracking workpieces across scans to avoid creating duplicates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param referenceIndex Reference index to match
     * @return Existing workpiece if found, null otherwise
     */
    public synchronized WorkpieceData findAtPosition(double x, double y, double z, int referenceIndex)
    {
        for (int i = 0; i < workpieces.size(); i++)
        {
            WorkpieceData wp = workpieces.get(i);
            if (wp.getReferenceIndex() == referenceIndex && wp.isAtPosition(x, y, z, positionToleranceMm))
            {
                log.debug("Found existing workpiece at position: id=" + wp.getId());
                return wp;
            }
        }
        return null;
    }

    /**
     * Adds or updates a workpiece. If a workpiece exists at the same position
     * with the same reference, updates it instead of creating a new one.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param rx Rotation X
     * @param ry Rotation Y
     * @param rz Rotation Z
     * @param score Vision score
     * @param referenceIndex Reference index
     * @return The workpiece (existing or new)
     */
    public synchronized WorkpieceData addOrUpdateWorkpiece(double x, double y, double z, double rx, double ry, double rz, double score, int referenceIndex)
    {
        // Try to find existing workpiece at this position
        WorkpieceData existing = findAtPosition(x, y, z, referenceIndex);

        if (existing != null)
        {
            // Update existing workpiece if it's been returned or is still available
            if (existing.getState() == WorkpieceState.RETURNED || existing.getState() == WorkpieceState.AVAILABLE)
            {
                existing.set(x, y, z, rx, ry, rz, score);
                existing.setState(WorkpieceState.AVAILABLE);
                log.info("Updated existing workpiece: id=" + existing.getId() + ", ref=" + referenceIndex + ", score=" + score);
                return existing;
            } else
            {
                // Workpiece is in use (PICKED, MEASURING, MEASURED) - create new one
                log.debug("Workpiece at position is in use (state=" + existing.getState() + "), creating new entry");
            }
        }

        // No existing workpiece found or existing one is in use - create new
        WorkpieceData wp = new WorkpieceData(x, y, z, rx, ry, rz, score);
        wp.setReferenceIndex(referenceIndex);
        workpieces.add(wp);
        log.debug("Added new workpiece to queue: id=" + wp.getId() + ", ref=" + referenceIndex + ", score=" + score);
        return wp;
    }
}
