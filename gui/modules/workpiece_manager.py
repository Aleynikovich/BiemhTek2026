import json


class WorkpieceManager:
    """Manages workpiece data and synchronization."""

    def __init__(self, tree_widget, canvas_widget, gripper_panel=None):
        self.tree = tree_widget
        self.canvas = canvas_widget
        self.gripper_panel = gripper_panel
        self.last_workpieces_data = []

    def update_data(self, workpieces_data):
        if isinstance(workpieces_data, str):
            try:
                workpieces = json.loads(workpieces_data)
            except json.JSONDecodeError:
                return
        else:
            workpieces = workpieces_data

        # Normalize states and split by location (on table vs held by gripper)
        normalized = []
        in_grippers = {1: None, 2: None, 3: None}
        available_on_table = []
        for wp in workpieces:
            wp = dict(wp)  # shallow copy to adjust presentation fields safely
            state = str(wp.get('state', 'AVAILABLE')).upper()
            gripper = wp.get('gripper')
            # Treat returned/measured on table as AVAILABLE when not in any gripper
            if state in ('MEASURED', 'RETURNED') and (not gripper or str(gripper) in ('', 'None', '0')):
                wp['state'] = 'AVAILABLE'
                state = 'AVAILABLE'
            # Route to grippers if being held or explicitly assigned
            if str(gripper).isdigit() and int(gripper) in (1, 2, 3):
                in_grippers[int(gripper)] = wp
            elif state in ('PICKED', 'MEASURING'):
                # Picked/measuring: hide from table even if gripper unknown
                pass
            else:
                available_on_table.append(wp)
            normalized.append(wp)

        self.last_workpieces_data = normalized
        # Update tree with normalized view
        self._update_tree(normalized)
        # Update canvas only with available items on the table
        self.canvas.update_visualization(available_on_table)
        # Update gripper panel if present
        if self.gripper_panel is not None:
            self.gripper_panel.update_grippers(in_grippers)

    def _update_tree(self, workpieces):
        self.tree.delete(*self.tree.get_children())
        for wp in workpieces:
            orientation = wp.get('orientation', 0)
            ori_symbol = "→" if orientation == 0 else "↻"
            ref_str = wp.get('referenceString', str(wp.get('reference', 'N/A')))
            self.tree.insert('', 'end', values=(
                str(wp.get('id', 'N/A'))[-8:],
                ref_str,
                ori_symbol,
                wp.get('state', 'N/A'),
                wp.get('gripper', 'N/A'),
                f"{wp.get('x', 0):.1f}",
                f"{wp.get('y', 0):.1f}",
                f"{wp.get('z', 0):.1f}",
                f"{wp.get('rx', 0):.1f}",
                f"{wp.get('ry', 0):.1f}",
                f"{wp.get('rz', 0):.1f}",
                f"{wp.get('score', 0):.2f}"
            ))

    def find_workpiece_by_short_id(self, short_id):
        short_id = str(short_id)
        for wp in self.last_workpieces_data:
            full_id = str(wp.get('id', ''))
            if full_id.endswith(short_id) or full_id == short_id:
                return wp
        return None
