/********************************************************************************/
/*                                                                              */
/*              PetalNodeComponent.java                                         */
/*                                                                              */
/*      Shaped node component                                                   */
/*                                                                              */
/********************************************************************************/



package edu.brown.cs.ivy.petal;

import edu.brown.cs.ivy.swing.SwingText;

import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class PetalNodeComponent extends JPanel implements PetalConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private PetalNodeShape  use_shape;
private transient Shape draw_shape;
private String          node_contents;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructore                                                            */
/*                                                                              */
/********************************************************************************/

PetalNodeComponent(PetalNodeShape sh)
{
   this(sh,null);
}


PetalNodeComponent(PetalNodeShape sh,String cnts) 
{
   node_contents = cnts;
   use_shape = sh;
   draw_shape = null;
   
   Dimension d = getNodeSize();
   setSize(d);
   setPreferredSize(d);
   setMinimumSize(d);
   if (sh == null) sh = PetalNodeShape.SQUARE;
   setupShape();
}



/********************************************************************************/
/*                                                                              */
/*      Drawing routines                                                        */
/*                                                                              */
/********************************************************************************/

@Override protected void paintComponent(Graphics g) 
{
   Graphics2D g2 = (Graphics2D) g;
   g2.setColor(getForeground());
   Rectangle r = getBounds();
   int tx = 0;
   int ty = 0;
   int tw = r.width;
   int th = r.height;
   
   Shape s = draw_shape;
   switch (use_shape) {
      case SQUARE :
      case RECTANGLE :
      default :
         r.x = r.y = 0;
         s = r;
         break;
      case TRIANGLE :
         tx = 5;
         ty = 10;
         tw -= 10;
         th -= 10;
         break;
      case TRIANGLE_DOWN :
         tx = 5;
         ty = 0;
         tw -= 10;
         th -= 10;
         break;
      case DIAMOND :
         tx = 10;
         ty = 10;
         tw -= 20;
         th -= 20;
         break;
      case PENTAGON :
         tx = 5;
         ty = 10;
         tw -= 10;
         th -= 10;
         break;
      case CIRCLE :
         Ellipse2D el = new Ellipse2D.Double();
         el.setFrame(0,0,r.width,r.height);
         s = el;
         tx = 10;
         ty = 10;
         tw -= 20;
         th -= 20;
         break;
    }
   
   if (s != null) {
      g2.setColor(getBackground());
      g2.fill(s);
      g2.setColor(Color.BLACK);
      g2.draw(s);
    }
   
   if (node_contents != null) {
      g2.setColor(Color.BLACK);
      Rectangle2D r2d = new Rectangle2D.Double(tx+1,ty+1,tw-2,th-2);
      SwingText.drawText(node_contents,g2,r2d);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Shape management methods                                                */
/*                                                                              */
/********************************************************************************/

private Dimension getNodeSize()
{
   if (node_contents == null) return new Dimension(20,20);
   JLabel lbl = new JLabel(node_contents);
   Dimension d = lbl.getPreferredSize();
   int w = d.width;
   int h = d.height;
   switch (use_shape) {
      case SQUARE :
      case RECTANGLE :
        break;
      case TRIANGLE :
      case TRIANGLE_DOWN :
         w += 10;
         h += 10;
         break;
      case CIRCLE :
         w += 20;
         h += 20;
         break;
      case DIAMOND :
         w += 20;
         h += 20;
         break;
      case PENTAGON :
         w += 10;
         h += 10;
         break;
    }
   
   w = Math.max(w,20);
   h = Math.max(h,20);
   return new Dimension(w,h);
}




private void setupShape()
{
   Rectangle r = getBounds();
   Polygon ps = null;
   
   switch (use_shape) {
      case SQUARE :
      case RECTANGLE :
      default :
         draw_shape = new Rectangle(0,0,r.width-1,r.height-1);
         break;
      case CIRCLE :
         if (node_contents == null) {
            draw_shape = new Ellipse2D.Double(0,0,r.width-1,r.height-1);
          }
         else {
            Path2D.Double p2d = new Path2D.Double();
            p2d.moveTo(10,10);
            p2d.quadTo(r.width/2,-10,r.width-10,10);
            p2d.quadTo(r.width+10,r.height/2,r.width-10,r.height-10);
            p2d.quadTo(r.width/2,r.height+10,10,r.height-10);
            p2d.quadTo(-10,r.height/2,10,10);
            p2d.closePath();
            draw_shape = p2d;
          }
         break;
      case TRIANGLE :
         ps = new Polygon();
         ps.addPoint(r.width/2,0);
         if (node_contents == null) {
            ps.addPoint(0,r.height);
            ps.addPoint(r.width,r.height);
          }
         else {
            ps.addPoint(5,10);
            ps.addPoint(0,r.height);
            ps.addPoint(r.width,r.height);
            ps.addPoint(r.width-5,10);
          }
         break;
      case TRIANGLE_DOWN :
         ps = new Polygon();
         ps.addPoint(0,0);
         if (node_contents == null) {
            ps.addPoint(r.width/2,r.height);
            ps.addPoint(r.width,0);
          }
         else {
            ps.addPoint(5,r.height-10);
            ps.addPoint(r.width/2,r.height);
            ps.addPoint(r.width-5,r.height-10);
            ps.addPoint(r.width,0);
          }
         break;
      case DIAMOND :
         ps = new Polygon();
         ps.addPoint(r.width/2,0);
         if (node_contents == null) {
            ps.addPoint(0,r.height/2);
            ps.addPoint(r.width/2,r.height);
            ps.addPoint(r.width,r.height/2);
          }
         else {
            ps.addPoint(10,10);
            ps.addPoint(0,r.height/2);
            ps.addPoint(10,r.height-10);
            ps.addPoint(r.width/2,r.height);
            ps.addPoint(r.width-10,r.height-10);
            ps.addPoint(r.width,r.height/2); 
            ps.addPoint(r.width-10,10);
          }
         break;
      case PENTAGON :
         ps = new Polygon();
         ps.addPoint(r.width/2,0);
         if (node_contents == null) {
            double a1 = Math.tan(Math.toRadians(54));
            int h1 = (int)(r.width / 2.0 / a1);
            double a2 = Math.tan(Math.toRadians(18));
            int h2 = (int)(a2 * (r.height - h1));
            ps.addPoint(r.width,h1);
            ps.addPoint(r.width - h2,r.height);
            ps.addPoint(h2,r.height);
            ps.addPoint(0,h1);
          }
         else {
            ps.addPoint(0,10);
            ps.addPoint(5,r.height);
            ps.addPoint(r.width-5,r.height);
            ps.addPoint(r.width,10);
          }
         break;
    }
   
   if (ps != null) draw_shape = ps;
}



/********************************************************************************/
/*                                                                              */
/*      Handle connections                                                      */
/*                                                                              */
/********************************************************************************/

Point findPortPoint(Point at,Point from) 
{
   switch (use_shape) {
      case SQUARE :
      case RECTANGLE :
         return PetalHelper.findPortPoint(getBounds(),at,from);
      case CIRCLE :
         if (node_contents == null) 
            return PetalHelper.findOvalPortPoint(getBounds(),at,from);
         else  
            return PetalHelper.findShapePortPoint(this,draw_shape,at,from);
      default :
      case DIAMOND :
      case TRIANGLE :
      case TRIANGLE_DOWN :
      case PENTAGON :
         break; 
    }
   if (draw_shape != null)
      return PetalHelper.findShapePortPoint(this,draw_shape,at,from);
   
   return PetalHelper.findPortPoint(getBounds(),at,from);
}



}       // end of class PetalNodeComponent




/* end of PetalNodeComponent.java */
