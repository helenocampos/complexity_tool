package com.mycompany.complexity.tool.mvn;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.mycompany.complexity.tool.mvn.Nodes.Edge;
import com.mycompany.complexity.tool.mvn.Nodes.IfNode;
import com.mycompany.complexity.tool.mvn.Nodes.Node;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

/**
 *
 * @author helenocampos
 */
public class Renderer {

    private int lastEdgeId;
    private Parser parser;
    private Tree<Node, Edge> g = new DelegateTree<>();
    private VisualizationViewer<Node, Edge> vv;
    private Node selectedNode;
    final ScalingControl scaler = new CrossoverScalingControl();

    public Renderer(Parser parser) {
        this.parser = parser;
        lastEdgeId = 0;
//        g.addVertex(1);
        g.addVertex(parser.getRoot());
        constructGraph(parser.getRoot());
    }
    
    public Renderer(Parser parser, Tree<Node, Edge> graph) {
        this.parser = parser;
        lastEdgeId = 0;
        this.g = graph;
        constructGraph(parser.getRoot());
    }

    private void constructGraph(Node actualNode) {  // lateral link controls if its possible to link node laterally

        if (!actualNode.isRendered() && !actualNode.isLocked()) {
            actualNode.setLocked(true);

            if (actualNode.getLeft() != null) {
                Edge edge = new Edge(lastEdgeId++, actualNode, actualNode.getLeft());
                getG().addEdge(edge, actualNode, actualNode.getLeft(), EdgeType.DIRECTED);
            }

            if (actualNode.getRight() != null) {
                Edge edge = new Edge(lastEdgeId++, actualNode, actualNode.getRight());
                getG().addEdge(edge, actualNode, actualNode.getRight(), EdgeType.DIRECTED);

            }

            if (actualNode.getLeft() != null) {

                constructGraph(actualNode.getLeft());

            }

            if (actualNode.getRight() != null) {

                constructGraph(actualNode.getRight());

            }

            actualNode.setRendered(true);
        }
    }

    public VisualizationViewer<Node, Edge> renderGraph(final JTextArea codeArea, final JTextArea statementDisplayArea, final MethodDeclaration method) {
        Layout<Node, Edge> layout;
//        if (parser.getNodes().size() > 15) {
//            layout = new TreeLayoutDois(getG());
//        } else {
            layout = new TreeLayout(getG(), parser.getNodes());
//        }

        vv = new VisualizationViewer<Node, Edge>(layout);

        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<Node, Edge>());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();

        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);

        vv.setGraphMouse(gm);
        vv.scaleToLayout(scaler);
        vv.addKeyListener(gm.getModeKeyListener());

        final PickedState<Node> pickedState = vv.getPickedVertexState();
        vv.scaleToLayout(new CrossoverScalingControl());
        
        pickedState.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                Object subject = e.getItem();
                if (subject instanceof Node) {
                    Node vertex = (Node) subject;
                    if (pickedState.isPicked(vertex)) {
                        setSelectedVertexStroke(vertex);
                        selectedNode = vertex;
                        
                        statementDisplayArea.setText(selectedNode.getStatementText());
                        int line = 0;
                        int finalLine = 0;
                        if (selectedNode.getBaseStatement() != null) {
                            line = selectedNode.getBaseStatement().getBeginLine() - method.getBeginLine();
                            finalLine = selectedNode.getBaseStatement().getEndLine() - method.getBeginLine();
                        } else if (selectedNode.getType().equals(Node.NodeType.IF)) {
                            IfNode node = (IfNode) selectedNode;
                            line = node.getCondition().getBeginLine() - method.getBeginLine();
                            finalLine = node.getCondition().getEndLine() - method.getBeginLine();
                        }

                        if (selectedNode.getType().equals(Node.NodeType.BLOCK)) {
                            selectCodeAreaLines(codeArea, line + 1, finalLine - 1);
                        } else if (selectedNode.getType().equals(Node.NodeType.LOOP_EXIT)) {
                            selectCodeAreaLines(codeArea, finalLine, finalLine);
                        } else {
                            selectCodeAreaLines(codeArea, line, finalLine);
                        }

                    } else {
                        vv.getRenderContext().setVertexStrokeTransformer(new ConstantTransformer(new BasicStroke()));
                    }
                }
            }
        });
        
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge, String>() {
                public String transform(Edge e) {
                    if(e.getSourceNode().getType().equals(Node.NodeType.IF)){
                        IfNode ifNode = (IfNode) e.getSourceNode();
                        if(ifNode.hasElse() && ifNode.getRight().equals(e.getDestinationNode())){
                            return "else";
                        }else{
                            if(ifNode.getLeft().equals(e.getDestinationNode())){
                                return "then";
                            }
                        }
                        
                    }
                    return "";
                }
            });

        return vv;
    }

    private void selectCodeAreaLines(JTextArea codeArea, int initialPosition, int finalPosition) {
        try {
            initialPosition = codeArea.getLineStartOffset(initialPosition);
            finalPosition = codeArea.getLineEndOffset(finalPosition);
            Highlighter highlighter = codeArea.getHighlighter();
            highlighter.removeAllHighlights();
            HighlightPainter painter
                    = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);

            highlighter.addHighlight(initialPosition, finalPosition, painter);

        } catch (BadLocationException ex) {
            Logger.getLogger(Renderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSelectedVertexStroke(final Node vertex) {
        Transformer<Node, Stroke> vertexStroke = new Transformer<Node, Stroke>() {
            public Stroke transform(Node i) {
                if (i == vertex) {
                    return new BasicStroke(3.0f);
                } else {
                    return new BasicStroke();
                }

            }
        };
        vv.getRenderContext().setVertexStrokeTransformer(vertexStroke);

    }

    public void fillPath(final Stack<Node> path) {
        Transformer<Node, Paint> vertexPaint
                = new Transformer<Node, Paint>() {
                    @Override
                    public Paint transform(Node i) {
                        if (Node.contains(path, i.getId())) {
                            return Color.BLUE;
                        } else {
                            return Color.RED;
                        }

                    }
                };
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

        Transformer<Edge, Paint> edgePaint = new Transformer<Edge, Paint>() {
            public Paint transform(Edge edge) {
                if (Edge.contains(edge, path)) {
                    return Color.BLUE;
                } else {
                    return Color.BLACK;
                }
            }
        };

        Transformer<Edge, Stroke> edgeStroke = new Transformer<Edge, Stroke>() {
            public Stroke transform(Edge edge) {
                if (Edge.contains(edge, path)) {
                    return new BasicStroke(4.0f);
                } else {
                    return new BasicStroke();
                }

            }
        };

        vv.getRenderContext().setEdgeArrowStrokeTransformer(edgeStroke);
        vv.getRenderContext().setArrowFillPaintTransformer(edgePaint);
        vv.getRenderContext()
                .setEdgeStrokeTransformer(edgeStroke);

        vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
        vv.getRenderContext()
                .setVertexFillPaintTransformer(vertexPaint);
    }

    public VisualizationViewer<Node, Edge> getVisualizationViewer() {
        return this.vv;
    }

    public Node getSelectedNode() {
        return this.selectedNode;
    }

    /**
     * @return the g
     */
    public Tree<Node, Edge> getG() {
        return g;
    }

    /**
     * @param g the g to set
     */
    public void setG(Tree<Node, Edge> g) {
        this.g = g;
    }
    
    public void setMoveMode(){
        DefaultModalGraphMouse mouse = (DefaultModalGraphMouse) vv.getGraphMouse();
        mouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
    }
    
    public void setSelectingMode(){
        DefaultModalGraphMouse mouse = (DefaultModalGraphMouse) vv.getGraphMouse();
        mouse.setMode(ModalGraphMouse.Mode.PICKING);
    }
    
    public void zoomIn(){
        scaler.scale(vv, 1.1f, vv.getCenter());
    }
    
    public void zoomOut(){
        scaler.scale(vv, 1/1.1f, vv.getCenter());
    }
}
