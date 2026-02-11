import math
import tkinter as tk


class GripperPanel(tk.Canvas):
    """Simple 2D visualization for three grippers holding workpieces."""

    def __init__(self, parent, **kwargs):
        super().__init__(parent, width=700, height=120, bg='#f8f9fb', highlightthickness=0, **kwargs)
        self._draw_frame()

    def _draw_frame(self):
        self.delete('all')
        w = int(self.cget('width'))
        h = int(self.cget('height'))
        # Title
        self.create_text(10, 12, text="Grippers", anchor='w', font=('Helvetica', 10, 'bold'), fill='#333')
        # Slot backgrounds
        margin = 10
        spacing = 10
        slot_w = (w - 2 * margin - 2 * spacing) // 3
        slot_h = h - 2 * margin - 20
        self.slots = []
        for i in range(3):
            x0 = margin + i * (slot_w + spacing)
            y0 = margin + 20
            x1 = x0 + slot_w
            y1 = y0 + slot_h
            # rounded-ish rectangle (simulate with 4 arcs + rects is heavy; keep simple with rect)
            self.create_rectangle(x0, y0, x1, y1, fill='white', outline='#c9d1d9', width=1)
            self.create_text(x0 + 8, y0 + 8, text=f"Gripper {i + 1}", anchor='nw', font=('Helvetica', 9, 'bold'),
                             fill='#0d6efd')
            self.slots.append((x0, y0, x1, y1))

    def update_grippers(self, in_grippers):
        """
        Draw miniature workpieces currently held by each gripper.
        in_grippers: dict like {1: wp_dict_or_None, 2: ..., 3: ...}
        """
        # Redraw frame and content region
        self._draw_frame()
        # Draw content inside each slot
        for i in range(3):
            slot = self.slots[i]
            wp = in_grippers.get(i + 1)
            if not wp:
                # draw subtle empty hint
                self.create_text((slot[0] + slot[2]) // 2, (slot[1] + slot[3]) // 2, text='— empty —', fill='#94a3b8',
                                 font=('Helvetica', 9, 'italic'))
                continue
            # Draw a small oriented rectangle representing the workpiece
            self._draw_workpiece_in_slot(slot, wp)

    def _draw_workpiece_in_slot(self, slot, wp):
        x0, y0, x1, y1 = slot
        cx = (x0 + x1) / 2
        cy = (y0 + y1) / 2 + 8
        # miniature size
        length = 60
        width = 24
        rz = float(wp.get('rz', 0))
        orientation = wp.get('orientation', 0)
        # colors by reference
        ref = wp.get('reference', 1)
        ref_colors = {1: '#FF4D4D', 2: '#8A2BE2', 3: '#3B82F6'}
        fill_color = ref_colors.get(ref, '#CBD5E1')
        outline = '#111827'
        # rotate
        ang = math.radians(-rz + (180 if orientation == 1 else 0))
        cos_a = math.cos(ang)
        sin_a = math.sin(ang)
        hl, hw = length / 2, width / 2
        corners = [(-hl, -hw), (hl, -hw), (hl, hw), (-hl, hw)]
        pts = []
        for px, py in corners:
            rx = px * cos_a - py * sin_a
            ry = px * sin_a + py * cos_a
            pts.extend([cx + rx, cy + ry])
        self.create_polygon(pts, fill=fill_color, outline=outline, width=2)
        # arrow indicating X+
        arrow_len = hl
        ax = arrow_len * math.cos(ang)
        ay = arrow_len * math.sin(ang)
        self.create_line(cx, cy, cx + ax, cy + ay, arrow=tk.LAST, fill='#F59E0B', width=2)
        # label with id tail
        wp_id = wp.get('id', '?')
        label = f"ID:{str(wp_id)[-4:]}"
        self.create_text(cx, y0 + 18, text=label, fill='#111827', font=('Helvetica', 9, 'bold'))
