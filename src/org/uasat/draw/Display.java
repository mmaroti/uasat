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
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
public class Display extends JComponent {
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;
	private static final Color BACKGROUND = new Color(0xF0F0F0);

	private final Graph graph;
	private ControlPanel control = new ControlPanel();

	private int node_radius = 4;

	private static final int DRAGSTATE_NONE = 0;
	private static final int DRAGSTATE_SELECT = 1;
	private static final int DRAGSTATE_MOVE = 2;
	private int dragState = DRAGSTATE_NONE;

	private Point mousePt = new Point();
	private Rectangle selectRect = new Rectangle();

	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Graph Display");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Display display = new Display(Graph.createN5());
				frame.add(display.control, BorderLayout.NORTH);
				frame.add(new JScrollPane(display), BorderLayout.CENTER);
				frame.getRootPane().setDefaultButton(display.control.defaultButton);
				frame.pack();
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
	}

	public Display(Graph graph) {
		this.graph = graph;
		this.setOpaque(true);

		MouseHandler handler = new MouseHandler();
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	@Override
	public void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());

		for (Edge edge : graph.getEdges())
			paintEdge(g, edge);

		for (Node node : graph.getNodes())
			paintNode(g, node);

		if (dragState == DRAGSTATE_SELECT) {
			g.setColor(Color.DARK_GRAY);
			g.drawRect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
		}
	}

	private void paintNode(Graphics2D graphics, Node node) {
		Point center = node.getCenter();

		if (node.isSelected())
			graphics.setColor(Color.RED);
		else
			graphics.setColor(Color.BLACK);

		graphics.drawOval(center.x - node_radius, center.y - node_radius, 2 * node_radius, 2 * node_radius);
	}

	private void paintEdge(Graphics2D graphics, Edge edge) {
		Node node1 = edge.getNode1();
		Node node2 = edge.getNode2();
		Point p1 = node1.getCenter();
		Point p2 = node2.getCenter();

		graphics.setColor(Color.BLACK);

		if (p1.distance(p2) > 2 * node_radius + 4) {
			double a = Math.atan2(p2.y - p1.y, p2.x - p1.x);

			int p1x = p1.x + (int) (Math.cos(a) * (node_radius + 2));
			int p1y = p1.y + (int) (Math.sin(a) * (node_radius + 2));
			int p2x = p2.x - (int) (Math.cos(a) * (node_radius + 2));
			int p2y = p2.y - (int) (Math.sin(a) * (node_radius + 2));

			graphics.drawLine(p1x, p1y, p2x, p2y);

			if (edge.isDisplayArrow()) {
				final double b = 0.35;
				final int r = node_radius * 2 + 3;

				int p3x = p2x - (int) (Math.cos(a + b) * r);
				int p3y = p2y - (int) (Math.sin(a + b) * r);
				int p4x = p2x - (int) (Math.cos(a - b) * r);
				int p4y = p2y - (int) (Math.sin(a - b) * r);

				graphics.fillPolygon(new int[] { p2x, p3x, p4x }, new int[] { p2y, p3y, p4y }, 3);
			}
		}
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		return event.getX() + "," + event.getY();
	}

	public Node findNode(Point point) {
		for (Node node : graph.getNodes())
			if (point.distance(node.getCenter()) <= node_radius + 1)
				return node;
		return null;
	}

	private class MouseHandler extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent event) {
			if (event.getClickCount() == 1 && event.getButton() == 1) {
				Node n = findNode(event.getPoint());
				if (n == null)
					graph.unselectAll();
				else if (event.isShiftDown())
					n.setSelected(!n.isSelected());
				else {
					graph.unselectAll();
					n.setSelected(true);
				}
				repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getButton() == 1)
				mousePt = event.getPoint();

			if (event.isPopupTrigger())
				showPopup(event);
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == 1 && dragState != DRAGSTATE_NONE) {
				dragState = DRAGSTATE_NONE;
				repaint();
			}

			if (event.isPopupTrigger())
				showPopup(event);
		}

		private void showPopup(MouseEvent e) {
			control.popup.show(e.getComponent(), e.getX(), e.getY());
		}

		Point delta = new Point();

		@Override
		public void mouseDragged(MouseEvent event) {
			if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
				if (dragState == DRAGSTATE_NONE) {
					Node n = findNode(mousePt);
					if (n != null) {
						if (!n.isSelected()) {
							graph.unselectAll();
							n.setSelected(true);
						}
						dragState = DRAGSTATE_MOVE;
					} else
						dragState = DRAGSTATE_SELECT;
				}

				if (dragState == DRAGSTATE_SELECT) {
					selectRect.setBounds(Math.min(mousePt.x, event.getX()), Math.min(mousePt.y, event.getY()),
						Math.abs(mousePt.x - event.getX()), Math.abs(mousePt.y - event.getY()));
					graph.select(selectRect);
				} else if (dragState == DRAGSTATE_MOVE) {
					delta.setLocation(event.getX() - mousePt.x, event.getY() - mousePt.y);
					graph.moveSlected(delta);
					mousePt = event.getPoint();
				}
				repaint();
			}
		}
	}

	private class ControlPanel extends JToolBar {
		private Action addNode = new AddNodeAction("Add");
		private Action delete = new DeleteAction("Delete");
		private Action clearAll = new ClearAllAction("Clear");
		private Action connect = new ConnectAction("Connect");

		private JButton defaultButton = new JButton(addNode);
		private JPopupMenu popup = new JPopupMenu();

		ControlPanel() {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setBackground(Color.LIGHT_GRAY);
			this.setFloatable(false);

			this.add(defaultButton);
			this.add(new JButton(delete));
			this.add(new JButton(clearAll));
			this.add(new JButton(connect));

			popup.add(new JMenuItem(addNode));
			popup.add(new JMenuItem(delete));
			popup.add(new JMenuItem(connect));
		}
	}

	private class AddNodeAction extends AbstractAction {
		public AddNodeAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			graph.unselectAll();
			Node n = new Node(mousePt.getLocation());
			mousePt.x += 10;
			mousePt.y += 10;
			n.setSelected(true);
			graph.add(n);
			repaint();
		}
	}

	private class ClearAllAction extends AbstractAction {
		public ClearAllAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			graph.clear();
			repaint();
		}
	}

	private class ConnectAction extends AbstractAction {
		public ConnectAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			List<Node> list = graph.getSelected();
			if (list.size() > 1) {
				for (int i = 0; i < list.size() - 1; ++i) {
					Node n1 = list.get(i);
					Node n2 = list.get(i + 1);
					graph.add(new Edge(n1, n2));
				}
			}
			repaint();
		}
	}

	private class DeleteAction extends AbstractAction {
		public DeleteAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			graph.removeSelected();
			repaint();
		}
	}
}
