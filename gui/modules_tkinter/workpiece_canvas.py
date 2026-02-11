import math
import tkinter as tk


class WorkpieceCanvas(tk.Canvas):
    """Specialized canvas for workpiece visualization."""

    def __init__(self, parent, **kwargs):
        super().__init__(parent, bg='white', **kwargs)
        self.draw_grid()

    def draw_grid(self):
        width = int(self.cget('width'))
        height = int(self.cget('height'))
        grid_size = 50
        for x in range(0, width + 1, grid_size):
            self.create_line(x, 0, x, height, fill='lightgray', width=1)
        for y in range(0, height + 1, grid_size):
            self.create_line(0, y, width, y, fill='lightgray', width=1)
        self.create_text(width - 20, height - 10, text="X", fill='black', font=('Arial', 10, 'bold'))
        self.create_text(10, 10, text="Y", fill='black', font=('Arial', 10, 'bold'))
        self.create_rectangle(1, 1, width - 1, height - 1, outline='black', width=2)

    def update_visualization(self, workpieces):
        self.delete('workpiece')
        canvas_width = 470
        canvas_height = 650
        wp_length = 80
        wp_width = 40
        ref_colors = {1: '#FF0000', 2: '#800080', 3: '#0000FF'}
        state_colors = {'AVAILABLE': '', 'PICKED': 'orange', 'MEASURING': 'purple', 'MEASURED': 'blue',
                        'RETURNED': 'gray'}

        for wp in workpieces:
            x = float(wp.get('x', 0))
            y = float(wp.get('y', 0))
            rx_deg = float(wp.get('rx', 0))
            ry_deg = float(wp.get('ry', 0))
            rz_deg = float(wp.get('rz', 0))
            ref = wp.get('reference', 1)
            state = wp.get('state', 'AVAILABLE')
            gripper = wp.get('gripper', '')
            wp_id = wp.get('id', '?')

            canvas_x = x + 150
            canvas_y = -y - 250

            if canvas_x < -100 or canvas_x > canvas_width + 100 or canvas_y < -100 or canvas_y > canvas_height + 100:
                continue

            fill_color = ref_colors.get(ref, '#CCCCCC')
            outline_color = state_colors.get(state, 'black')
            outline_width = 3 if state == 'PICKED' else 2

            # Rotation math: Rz -> Ry -> Rx
            # We want to find the projection of the local X+ and Y+ axes onto the XY plane
            az = math.radians(rz_deg)
            ay = math.radians(ry_deg)
            ax = math.radians(rx_deg)

            # Local X+ unit vector after rotations:
            # ux = [cos(az)cos(ay), sin(az)cos(ay), -sin(ay)]
            ux_x = math.cos(az) * math.cos(ay)
            ux_y = math.sin(az) * math.cos(ay)

            # Local Y+ unit vector after rotations:
            # uy = [cos(az)sin(ay)sin(ax) - sin(az)cos(ax), sin(az)sin(ay)sin(ax) + cos(az)cos(ax), cos(ay)sin(ax)]
            uy_x = math.cos(az) * math.sin(ay) * math.sin(ax) - math.sin(az) * math.cos(ax)
            uy_y = math.sin(az) * math.sin(ay) * math.sin(ax) + math.cos(az) * math.cos(ax)

            # Rectangle corners calculation using projected basis
            half_l, half_w = wp_length / 2, wp_width / 2
            corners_basis = [
                (-half_l, -half_w), (+half_l, -half_w), (+half_l, +half_w), (-half_l, +half_w)
            ]
            rotated_corners = []
            for cl, cw in corners_basis:
                px = cl * ux_x + cw * uy_x
                py = cl * ux_y + cw * uy_y
                # canvas_y increases downward, so we subtract py from canvas_y
                rotated_corners.extend([canvas_x + px, canvas_y - py])

            self.create_polygon(rotated_corners, fill=fill_color, outline=outline_color, width=outline_width,
                                tags='workpiece')

            orientation = wp.get('orientation', 0)
            # Arrow follows local X+ (or X- if inverted)
            flip = -1 if orientation == 1 else 1
            arrow_dx = (wp_length / 2) * ux_x * flip
            arrow_dy = (wp_length / 2) * ux_y * flip
            self.create_line(canvas_x, canvas_y, canvas_x + arrow_dx, canvas_y - arrow_dy, arrow=tk.LAST, fill='yellow',
                             width=2, tags='workpiece')

            rev_rad = wp_length / 2 + wp_width / 4
            self.create_oval(canvas_x - rev_rad, canvas_y - rev_rad, canvas_x + rev_rad, canvas_y + rev_rad,
                             outline='red', dash=(2, 2), width=1, tags='workpiece')

            label = f"ID:{str(wp_id)[-4:]}"
            if gripper and gripper != 'None':
                label += f"\nG:{gripper}"
            self.create_text(canvas_x, canvas_y, text=label, fill='white', font=('Arial', 8, 'bold'), tags='workpiece')

        # Legend
        lx, ly = 10, 350
        self.create_text(lx, ly, text="Legend:", anchor='w', font=('Arial', 9, 'bold'), tags='workpiece')
        ly += 15
        for ref, color in ref_colors.items():
            self.create_rectangle(lx, ly, lx + 15, ly + 10, fill=color, outline='black', tags='workpiece')
            self.create_text(lx + 20, ly + 5, text=f"Ref {ref}", anchor='w', font=('Arial', 8), tags='workpiece')
            ly += 12
