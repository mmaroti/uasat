/**
 * Copyright (C) Miklos Maroti, 2016
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.uasat.draw;

import java.awt.*;

public class Node {
	private final Point center;
	private final String label;
	private int radius = 4;

	private int state = 0;
	private final static int STATE_SELECTED = 0x01;
	private final static int STATE_FIXED_X = 0x02;
	private final static int STATE_FIXED_Y = 0x04;

	private double forcex;
	private double forcey;

	public Node() {
		this.center = new Point();
		this.label = null;
	}

	public Node(String label) {
		this.center = new Point();
		this.label = label;
	}

	public Node(Point center) {
		this.center = center;
		this.label = null;
	}

	public Node(Point center, String label) {
		this.center = center;
		this.label = label;
	}

	public void draw(Graphics2D gaphics) {
		gaphics.setColor(Color.BLACK);
		gaphics.fillOval(center.x - radius, center.y - radius, 2 * radius + 1, 2 * radius + 1);

		if (isSelected()) {
			gaphics.setColor(Color.RED);
			gaphics.drawOval(center.x - radius - 3, center.y - radius - 3, 2 * radius + 6, 2 * radius + 6);
		}
	}

	public void resetForces() {
		forcex = 0.0;
		forcey = 0.0;
	}

	public void updateForces(double x, double y) {
		forcex += x;
		forcey += y;
	}

	public void applyForces(double speed) {
		center.x += forcex * speed;
		center.y += forcey * speed;
	}

	public Point getCenter() {
		return center;
	}

	public String getLabel() {
		return label;
	}

	public int getRadius() {
		return radius;
	}

	public void move(Point offset) {
		center.x += offset.x;
		center.y += offset.y;
	}

	public boolean contains(Point point) {
		return point.distance(center) <= radius + 1;
	}

	public boolean isSelected() {
		return (state | STATE_SELECTED) != 0;
	}

	public void setSelected(boolean selected) {
		if (selected)
			state |= STATE_SELECTED;
		else
			state &= ~STATE_SELECTED;
	}
}
