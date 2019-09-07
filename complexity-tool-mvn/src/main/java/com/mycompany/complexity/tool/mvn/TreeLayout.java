/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 9, 2005
 */
package com.mycompany.complexity.tool.mvn;

import com.mycompany.complexity.tool.mvn.Nodes.Node;
import com.mycompany.complexity.tool.mvn.Nodes.Node.NodeType;
import edu.uci.ics.jung.algorithms.layout.Layout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2
 *
 */
public class TreeLayout<V extends Comparable, E> implements Layout<V, E> {

    protected Dimension size = new Dimension(600, 600);
    protected Forest<V, E> graph;
    protected Map<V, Integer> basePositions = new HashMap<V, Integer>();
    protected List<Node> nodeList = new ArrayList<>();

    protected Map<V, Point2D> locations
            = LazyMap.decorate(new HashMap<V, Point2D>(),
                    new Transformer<V, Point2D>() {
                public Point2D transform(V arg0) {
                    return new Point2D.Double();
                }
            });

    protected transient Set<V> alreadyDone = new HashSet<V>();
    protected transient Set<V> alreadyCalculated = new HashSet<V>();

    protected Set<V> alreadyCalculatedY = new HashSet<>();
    protected int maxHeight;

    /**
     * The default horizontal vertex spacing. Initialized to 50.
     */
    public static int DEFAULT_DISTX = 50;

    /**
     * The default vertical vertex spacing. Initialized to 50.
     */
    public static int DEFAULT_DISTY = 50;

    /**
     * The horizontal vertex spacing. Defaults to {@code DEFAULT_XDIST}.
     */
    protected int distX = 50;

    /**
     * The vertical vertex spacing. Defaults to {@code DEFAULT_YDIST}.
     */
    protected int distY = 50;

    protected transient Point m_currentPoint = new Point();

    /**
     * Creates an instance for the specified graph with default X and Y
     * distances.
     */
    public TreeLayout(Forest<V, E> g, List<Node> list) {
        this(g, DEFAULT_DISTX, DEFAULT_DISTY, list);
    }

    /**
     * Creates an instance for the specified graph and X distance with default Y
     * distance.
     */
    public TreeLayout(Forest<V, E> g, int distx, List<Node> list) {
        this(g, distx, DEFAULT_DISTY, list);
    }

    /**
     * Creates an instance for the specified graph, X distance, and Y distance.
     */
    public TreeLayout(Forest<V, E> g, int distx, int disty, List<Node> list) {
        if (g == null) {
            throw new IllegalArgumentException("Graph must be non-null");
        }
        if (distx < 1 || disty < 1) {
            throw new IllegalArgumentException("X and Y distances must each be positive");
        }
        this.graph = g;
        this.distX = distx;
        this.distY = disty;
        this.nodeList = list;
        this.size = new Dimension(list.size() * 50, 600);
        this.maxHeight = calculateDimensionY(TreeUtils.getRoots(graph)) / 4;
        buildTree();
    }

    private Collection<V> getOrderedSucessors(V vertice) {
        Collection<V> sucessors = graph.getSuccessors(vertice);
        List<V> sucessorsList = new ArrayList<>(sucessors);
        Collections.sort(sucessorsList);
        return sucessorsList;
    }

    protected void buildTree() {
        this.m_currentPoint = new Point(size.width / 2, 20);
        int maxNumberOfParents = 0;
        Collection<V> roots = TreeUtils.getRoots(graph);
        if (roots.size() > 0 && graph != null) {
            for (V v : roots) {
                calculateDimensionX(v);
                //m_currentPoint.x += this.basePositions.get(v) / 2 + this.distX;
                buildTree(v, this.m_currentPoint.x, maxNumberOfParents, false);
            }
        }
        this.size = new Dimension(lastDimensionX(), lastDimensionY());
    }

    protected int buildTree(V v, int x, int distance, boolean conditionalParent) {
        int initialDistance = 0;
        if (!alreadyDone.contains(v)) {
            alreadyDone.add(v);

            Node actualNode = null;
            actualNode = (Node) v;

            this.m_currentPoint.x += this.distX;

            if ((actualNode.getType() == NodeType.IF)
                    || (actualNode.getType() == NodeType.CASE)) {
                if (actualNode.getLeft() != null && actualNode.getRight() != null) {
                    initialDistance += buildTree((V) actualNode.getRight(), x + this.distX, initialDistance + this.distX, true);
                    initialDistance += buildTree((V) actualNode.getLeft(), x - this.distX, initialDistance + this.distX, true);
                } else if (actualNode.getLeft() != null && actualNode.getRight() == null) {
                    initialDistance += buildTree((V) actualNode.getLeft(), x, initialDistance + this.distX, true);
                } else {
                    initialDistance += buildTree((V) actualNode.getRight(), x, initialDistance + this.distX, true);
                }
            } else if ((actualNode.getType() == NodeType.FOR)
                    || (actualNode.getType() == NodeType.FOREACH)
                    || (actualNode.getType() == NodeType.WHILE)
                    || (actualNode.getType() == NodeType.CASE_BLOCK)
                    || (actualNode.getType() == NodeType.BLOCK)) {
                initialDistance += buildTree((V) actualNode.getLeft(), x, initialDistance, false);
                if (actualNode.getRight() != null) {
                    initialDistance += buildTree((V) actualNode.getRight(), x + distX, initialDistance + this.distX, false);
                }
            } else if ((actualNode.getType() == NodeType.EXIT)
                    || (actualNode.getType() == NodeType.LOOP_EXIT)) {
                this.m_currentPoint.y += this.distY;
            } else {
                System.err.println("Erros! Milhões de erros");
            }
            this.m_currentPoint.x = x - initialDistance;
            setCurrentPositionFor(v);
            if ((actualNode.getType() == NodeType.EXIT)
                    || (actualNode.getType() == NodeType.LOOP_EXIT)) {
                this.m_currentPoint.y -= this.distY;
            }
            if (conditionalParent) {
                initialDistance -= this.distX;
            }
            this.m_currentPoint.y -= this.distY;
        }
        return initialDistance;
    }

    private int calculateDimensionX(V v) {

        int size = 0;
        int childrenNum = graph.getSuccessors(v).size();

        if (childrenNum != 0) {
            for (V element : getOrderedSucessors(v)) {
                if (!alreadyCalculated.contains(element)) {
                    alreadyCalculated.add(element);
                    size += calculateDimensionX(element) + distX;
                }
            }
        }
        size = Math.max(0, size - distX);
        basePositions.put(v, size);

        return size;
    }

    private int calculateDimensionX(Collection<V> roots) {

        int size = 0;
        for (V v : roots) {
            int childrenNum = graph.getSuccessors(v).size();

            if (childrenNum != 0) {
                for (V element : getOrderedSucessors(v)) {
                    if (!alreadyCalculated.contains(element)) {
                        alreadyCalculated.add(element);
                        size += calculateDimensionX(element) + distX;
                    }
                }
            }
            size = Math.max(0, size - distX);
            basePositions.put(v, size);
        }

        return size;
    }

    private int calculateDimensionY(V v) {
        int size = 0;
        int childrenNum = graph.getSuccessors(v).size();
        if (childrenNum != 0) {
            for (V element : getOrderedSucessors(v)) {
                if (!alreadyCalculatedY.contains(element)) {
                    alreadyCalculatedY.add(element);
                    size += calculateDimensionY(element) + this.distY;
                }
            }
        }
        size = Math.max(0, size - this.distY);
        alreadyCalculatedY.remove(v);
        return size;
    }

    private int calculateDimensionY(Collection<V> roots) {
        int size;
        List<Integer> maxSize = new ArrayList<>();
        for (V v : roots) {
            size = 0;
            int childrenNum = graph.getSuccessors(v).size();

            if (childrenNum != 0) {
                for (V element : getOrderedSucessors(v)) {
                    if (!alreadyCalculatedY.contains(element)) {
                        alreadyCalculatedY.add(element);
                        size += calculateDimensionY(element) + this.distY;
                    }
                }
            }
            size = Math.max(0, size - this.distY);
            maxSize.add(size);
        }
        return Collections.max(maxSize);
    }

    /**
     * This method is not supported by this class. The size of the layout is
     * determined by the topology of the tree, and by the horizontal and
     * vertical spacing (optionally set by the constructor).
     */
    public void setSize(Dimension size) {
        throw new UnsupportedOperationException("Size of TreeLayout is set"
                + " by vertex spacing in constructor");
    }

    protected void setCurrentPositionFor(V vertex) {
        int x = m_currentPoint.x;
        int y = m_currentPoint.y;
        if (x < 20) {
            size.width -= x;
            moveNodes(abs(x) + 10, 0);
            m_currentPoint.x += abs(x) + 10;
        }

        if (x > size.width - distX) {
            size.width = x + distX;
        }

        if (y < 0) {
            size.height -= y;
            moveNodes(0, abs(y) + 10);
            m_currentPoint.y += abs(y) + 10;
        }
        if (y > size.height - distY) {
            size.height = y + distY;
        }
        locations.get(vertex).setLocation(m_currentPoint);
    }

    public void moveNodes(int x, int y) {
        Point2D p;
        for (V value : locations.keySet()) {
            p = locations.get(value);
            p.setLocation(p.getX() + x, p.getY() + y);
            locations.get(value).setLocation(p);
        }
    }

    public int lastDimensionX() {
        Point2D p;
        int maxX = 0;
        for (V value : locations.keySet()) {
            p = locations.get(value);
            if (p.getX() > maxX) {
                maxX = (int) p.getX() + 1;
            }
        }
        return maxX;
    }

    public int lastDimensionY() {
        Point2D p;
        int maxY = 0;
        for (V value : locations.keySet()) {
            p = locations.get(value);
            if (p.getY() > maxY) {
                maxY = (int) p.getY() + 1;
            }
        }
        return maxY;
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public Dimension getSize() {
        return size;
    }

    public void initialize() {

    }

    public boolean isLocked(V v) {
        return false;
    }

    public void lock(V v, boolean state) {
    }

    public void reset() {
    }

    public void setGraph(Graph<V, E> graph) {
        if (graph instanceof Forest) {
            this.graph = (Forest<V, E>) graph;
            buildTree();
        } else {
            throw new IllegalArgumentException("graph must be a Forest");
        }
    }

    public void setInitializer(Transformer<V, Point2D> initializer) {
    }

    /**
     * Returns the center of this layout's area.
     */
    public Point2D getCenter() {
        return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
    }

    public void setLocation(V v, Point2D location) {
        locations.get(v).setLocation(location);
    }

    public Point2D transform(V v) {
        return locations.get(v);
    }
}
