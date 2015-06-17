package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Cylinder;


  class WayPoint
  {
        private Node wayNodeName;
        public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI/2,   new Vector3f(1,0,0));

      
        WayPoint(int num, float wayX, float wayY, AssetManager gameAssetMan)
        {
            BitmapFont guiFont = gameAssetMan.loadFont("Interface/Fonts/Default.fnt");
            BitmapText wayName = new BitmapText( guiFont, false );
            Material blueMat = new Material(gameAssetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            blueMat.setColor("Color", new ColorRGBA(0f,0f,1f, 0.3f));
            
            wayName.setSize(2f);
            wayName.setText( "Waypoint: " + num);
            float textWidthTopName = wayName.getLineWidth() + 15;
            float textOffsetTopName = textWidthTopName / 2;
            wayName.setBox( new Rectangle(-textOffsetTopName,0, textWidthTopName, wayName.getHeight()) );
            wayName.setColor( new ColorRGBA( 0, 0, 255, 1 ) );
            wayName.setAlignment( BitmapFont.Align.Center );
            wayName.setQueueBucket( RenderQueue.Bucket.Transparent );
            BillboardControl waybc = new BillboardControl();
            waybc.setAlignment( BillboardControl.Alignment.Screen );
            wayName.addControl(waybc);
            wayNodeName = new Node( "WayNodeName" + num );
            wayNodeName.setLocalTranslation( wayX, 7, wayY );       
            wayNodeName.attachChild( wayName ); 

            Cylinder cyl = new Cylinder(20,20,1.0f,7.0f,true);
            Geometry geomC = new Geometry("", cyl);
            geomC.setLocalTranslation(0, -7, 0 );
            geomC.rotate(PITCH090);
            geomC.setMaterial(blueMat);
            wayNodeName.attachChild(geomC);

        }
        
        public Node getNode()
        {
            return wayNodeName;
        }
  }