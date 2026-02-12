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
            
            # MEASURING and MEASURED workpieces are held by gripper 3 (measuring machine)
            if state in ('MEASURING', 'MEASURED'):
                if not gripper or str(gripper) not in ('3',):
                    # Ensure gripper is set to 3 for measuring machine
                    wp['gripper'] = '3'
                    gripper = '3'
            
            # Display gripper location in state if workpiece is held by a gripper
            if gripper and str(gripper) not in ('', 'None', '0', 'null'):
                if str(gripper).isdigit() and int(gripper) in (1, 2, 3):
                    wp['state'] = f'In Gripper {gripper}'
                    state = wp['state']
            
            # Route to grippers if being held
            if gripper and str(gripper).isdigit() and int(gripper) in (1, 2, 3):
                in_grippers[int(gripper)] = wp
            elif 'In Gripper' in state:
                # Workpiece is in a gripper - hide from table
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
        # Preserve current selection during refresh
        selected_items = self.tree.selection()
        selected_short_id = None
        if selected_items:
            try:
                # Get the short ID (first column) of the selected item
                values = self.tree.item(selected_items[0])['values']
                if values and len(values) > 0:
                    selected_short_id = str(values[0])
            except (IndexError, KeyError, TypeError):
                # If we can't get the selected ID, just continue without preserving selection
                pass
        
        self.tree.delete(*self.tree.get_children())
        item_to_select = None
        for wp in workpieces:
            orientation = wp.get('orientation', 0)
            ori_symbol = "→" if orientation == 0 else "↻"
            ref_str = wp.get('referenceString', str(wp.get('reference', 'N/A')))
            short_id = str(wp.get('id', 'N/A'))[-8:]
            item_id = self.tree.insert('', 'end', values=(
                short_id,
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
            # Track item to re-select if it matches the previously selected short ID
            if selected_short_id and short_id == selected_short_id:
                item_to_select = item_id
        
        # Restore selection if the item still exists
        if item_to_select:
            try:
                self.tree.selection_set(item_to_select)
                self.tree.see(item_to_select)
            except Exception:
                # If restoring selection fails, just continue
                pass

    def find_workpiece_by_short_id(self, short_id):
        short_id = str(short_id)
        for wp in self.last_workpieces_data:
            full_id = str(wp.get('id', ''))
            if full_id.endswith(short_id) or full_id == short_id:
                return wp
        return None
