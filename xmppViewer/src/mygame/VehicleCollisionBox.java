package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.math.FastMath;

  class VehicleCollisionBox
  {

      private String vehicleName;
  //    public BoundingBox boxVolume;
      private Vector3f myStart,myFin, myCent,zeroVec;
      private Float myDistance = 0f;
      private Double myAngle = 0d;
      Quaternion myRotAngle;
      Material redMat;
      Node holderNode;
      Box newBox;
      
      
      public VehicleCollisionBox(AssetManager gameAssetMan, String vName)
      {
          vehicleName = vName;
          myStart = new Vector3f(0f,0f,0f);
          myFin = new Vector3f(0f,0f,0f); 
          myCent = new Vector3f(0f,0f,0f);
        redMat = new Material(gameAssetMan, "Common/MatDefs/Misc/Unshaded.j3md");;
        redMat.setColor("Color", new ColorRGBA(0f,0f,1f, 0.2f));
        redMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        holderNode = new Node(vehicleName);
        zeroVec = new Vector3f(0.f,0.f,0.f);
        newBox = new Box(new Vector3f(0.f,0.f,0.f), 0,1,1.5f);
        System.out.println("initialised");
        myRotAngle = new Quaternion();
        myRotAngle.fromAngles(0, myAngle.floatValue(), 0);
      }
      
      public void update(float x1,float y1, float z1, float x2, float y2, float z2)
      {
          
            myStart.x=x1;
            myStart.y=y1;
            myStart.z=z1;
            myFin.x=x2;
            myFin.y=y2;
            myFin.z=z2;
	    myCent = FastMath.interpolateLinear(0.5f, myStart,myFin);
            //myCent = myCent.interpolate(myStart,myFin, 0.5f);
            myDistance = myStart.distance(myFin);
            myAngle = -Math.atan2((myFin.z-myStart.z), (myFin.x-myStart.x));
            myRotAngle.fromAngles(0, myAngle.floatValue(), 0);
      }
      
      public String getVehName()
      {
           return vehicleName;
      }
      
      public void update(Vector3f start, Vector3f end)
      {
            myStart=start;
            myFin=end;
            myCent = FastMath.interpolateLinear(0.5f, myStart,myFin);
            myDistance = myStart.distance(myFin);
            myAngle = -Math.atan2((myFin.z-myStart.z), (myFin.x-myStart.x));
            myRotAngle.fromAngles(0, myAngle.floatValue(), 0);
      }
      
      public void updateRender()
      {
            
            newBox.updateGeometry(zeroVec, getDistance(),1,1.5f);
            Geometry newGeom = new Geometry(vehicleName, newBox);
                
            newGeom.setLocalRotation(getQuatAngle());
            newGeom.setMaterial(redMat);
            newGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
            
            holderNode.detachAllChildren();
            holderNode.attachChild(newGeom);
            holderNode.setLocalTranslation(myCent);
      }
      
      public Node getMyNode()
      {
          System.out.println("getMyNode called for " + holderNode.toString());
          return holderNode;
      }
      
      public float getDistance()
      {
          return myDistance;
      }
      
      public  Quaternion getQuatAngle()
      {
          return myRotAngle;
      }
      
      public Vector3f getStart()
      {
          return myStart;
      }
      
      public Vector3f getEnd()
      {
          return myFin;
      }
  }
