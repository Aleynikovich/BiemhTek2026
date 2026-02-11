package biemhTekniker.data;

import biemhTekniker.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe queue for managing multiple workpieces.
 * Supports the pick-measure-return lifecycle.
 */
public class WorkpieceQueue
{
    private static final Logger log = Logger.getLogger(WorkpieceQueue.class);
    private final List<WorkpieceData> workpieces = new ArrayList<WorkpieceData>();

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
}
