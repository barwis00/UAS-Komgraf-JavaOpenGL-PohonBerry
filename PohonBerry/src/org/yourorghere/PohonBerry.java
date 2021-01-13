package org.yourorghere;

import com.sun.opengl.util.Animator;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

public class PohonBerry implements GLEventListener, MouseListener, MouseMotionListener
{
      boolean is_moving=false;

      // rotation angles, set in mousedragged
      // used in display
      float rotx=0.0f;
      float rotz=0.0f;
      float lastx=0.0f;
      float lastz=0.0f;

      // randomizing the tree
      public static long m_rand_seed=69171713;
      Random rand_rot = new Random(m_rand_seed);

      public static void main(String[] args) {
        final Frame frame = new Frame("Pohon Berry");
        GLCanvas canvas = new GLCanvas();

        PohonBerry r=new PohonBerry();

        canvas.addGLEventListener(r);
        canvas.addMouseListener(r);
        canvas.addMouseMotionListener(r);
        frame.add(canvas);
        frame.setSize(640, 480);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }



    public void init(GLAutoDrawable drawable) {
      GL gl = drawable.getGL();
      // prepare ligthsource
      float ambient[] = {0.4f,0.4f,0.4f,1.0f };
      float diffuse[] = {1.0f,1.0f,1.0f,1.0f };
      float position[] = {1.0f,1.0f,1.0f,0.0f };

      gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient,0);
      gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuse,0);
      gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position,0);

      gl.glEnable(GL.GL_LIGHT0);
      gl.glEnable(GL.GL_LIGHTING);

      // smooth the drawing
      gl.glShadeModel(GL.GL_SMOOTH);

      // depth sorting
      gl.glEnable(GL.GL_DEPTH_TEST);
      gl.glDepthFunc(GL.GL_LESS);


      // set background to light gray
      gl.glClearColor(0.4f, 0.7f, 0.3f, 1.0f);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        GL gl = drawable.getGL();
        GLU glu = new GLU();

        if (height <= 0) { 
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective( 45.0, (float) width/height, 1.0, 500.0 );
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    /* display */
    public void display(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT |
                   GL.GL_DEPTH_BUFFER_BIT |
                   GL.GL_ACCUM_BUFFER_BIT);

        
        gl.glLoadIdentity();

        glu.gluLookAt(  30.0f,0.0f,10.0f,
                        0.0f,0.0f,0.0f,  
                        0.0f,0.0f,1.0f); 

        // take it away
        gl.glTranslatef(0.0f,0.0f,-10.0f);
        // rotate around x-axis
        gl.glRotatef(rotx,1.0f,0.0f,0.0f);
        // rotate around z-axis
        gl.glRotatef(rotz,0.0f,0.0f,1.0f);

        
        rand_rot.setSeed(m_rand_seed);
        setStemMaterial(gl);
        DrawOneBranch(gl,0.3f, 3, 6);
        // Flush all drawing operations to the graphics card
        gl.glFlush();
    }
    /* eof display */

    public void displayChanged(GLAutoDrawable drawable, 
                               boolean modeChanged, 
                               boolean deviceChanged) { }

    // new tree on right click
    public void mouseClicked(MouseEvent e) 
    {
        if(e.getButton()==MouseEvent.BUTTON3)
            m_rand_seed=rand_rot.nextLong();
    }

    public void mousePressed(MouseEvent e)
    {
      lastx = e.getX();
      lastz = e.getY();
      is_moving=true;
    }

    public void mouseReleased(MouseEvent e) 
    {
         is_moving=false;
         e.getComponent().repaint();
    }
   
    public void mouseDragged(MouseEvent e)
    {
      int x = e.getX();
      int y = e.getY();
      Dimension dim = e.getComponent().getSize();
      int width = dim.width;
      int height = dim.height;

      rotz += (90.0f*((float)(x-lastx)/(float)(width)));
      rotx += (90.0f*((float)(y-lastz)/(float)(height)));

      lastx = x;
      lastz = y;
      e.getComponent().repaint();
    }

    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseMoved(MouseEvent e) { } 


    /* draw a branch */
    public void DrawOneBranch(GL gl, float radius, int branches, int depth) 
    {
      GLU glu=new GLU();
      GLUquadric glpQ=glu.gluNewQuadric();

      if(depth==0)
      {
        // end of a branch
        // dont use time on berrys when manipulating
        if(!is_moving)
        {

          setBerryMaterial(gl);
          glu.gluSphere(glpQ,5*radius,5,5);
          setStemMaterial(gl);
        }
        glu.gluDeleteQuadric(glpQ);
        return;
      }
      // branch is set to 20 times the radius
      float len=radius*20;

      if(!is_moving)
        glu.gluCylinder(glpQ,radius,radius,len,10,1);
      else
      {
        // speed up with simple lines while manipulating
        gl.glBegin(GL.GL_LINES);
        gl.glVertex3f(0.0f,0.0f,0.0f);
        gl.glVertex3f(0.0f,0.0f,len);
        gl.glEnd();
      }

      gl.glTranslatef(0.0f,0.0f,len);
      // to end of the branch
      for(int bix=0;bix<branches;bix++)
      {
        // do each subtree
        gl.glPushMatrix();

        gl.glRotatef(1.0f+(float)rand_rot.nextFloat()%360.0f,0.0f,0.0f,1.0f);
        gl.glRotatef(-90.0f*(1.0f+(float)rand_rot.nextFloat()%90.0f),1.0f,0.0f,0.0f);
        gl.glRotatef(-90.0f*(1.0f+(float)rand_rot.nextFloat()%90.0f),0.0f,1.0f,0.0f);

        DrawOneBranch(gl,radius*0.8f,branches,depth-1);

        gl.glPopMatrix();
        glu.gluDeleteQuadric(glpQ);
      }
    }

    /* material */
    private void setStemMaterial(GL gl)
    {
      //Set material for stem
      float amb[]={0.3f,0.3f,0.0f,1.0f};
      float diff[]={0.5f,0.5f,0.0f,1.0f};
      float spec[]={0.6f,0.6f,0.5f,1.0f};
      float shine=0.25f;
      gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT,amb,0);
      gl.glMaterialfv(GL.GL_FRONT,GL.GL_DIFFUSE,diff,0);
      gl.glMaterialfv(GL.GL_FRONT,GL.GL_SPECULAR,spec,0);
      gl.glMaterialf(GL.GL_FRONT,GL.GL_SHININESS,shine*128.0f);
    }
    
    private void setBerryMaterial(GL gl)
    {
      //Set material for berry
      float amb[]={0.4f,0.0f,0.0f,1.0f};
      float diff[]={0.5f,0.0f,0.0f,1.0f};
      float spec[]={0.7f,0.6f,0.5f,1.0f};
      float shine=0.25f;
      gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT,amb,0);
      gl.glMaterialfv(GL.GL_FRONT,GL.GL_DIFFUSE,diff,0);
      gl.glMaterialfv(GL.GL_FRONT,GL.GL_SPECULAR,spec,0);
      gl.glMaterialf(GL.GL_FRONT,GL.GL_SHININESS,shine*128.0f);
    }

    
}

