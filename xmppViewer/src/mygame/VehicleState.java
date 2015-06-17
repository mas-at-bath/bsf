/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.font.Rectangle;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;

import math.geom2d.*;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author vin
 */
class VehicleState {

    boolean deadReckon = false;
    private Node vehicleNode = new Node();
    private Node jasonStateNode = new Node();
    private long lastUpdate = System.currentTimeMillis();
    public String vehicleName = "not used";
    private float inferredSpeed = 0.0f;
    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private float orientation = 0.0f;
    private Quaternion rotAngle = new Quaternion();
    public BitmapText label1, label2, label3, label4;
    public String info1 = " ";
    public String info2 = " ";
    public String info3 = " ";
    public String info4 = " ";
    public static final Quaternion ROLL045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0, 0, 1));
    public static final Quaternion YAW045n = new Quaternion().fromAngleAxis(-FastMath.PI / 4, new Vector3f(0, 1, 0));
    public static final Quaternion YAW045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 1, 0));
    public static final Quaternion YAW090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 1, 0));
    public static final Quaternion YAW180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0));
    public static final Quaternion YAW270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0, 1, 0));
    public static final Quaternion PITCH045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(1, 0, 0));
    public static final Quaternion UPRIGHT = new Quaternion().fromAngleAxis(-FastMath.PI / 2, new Vector3f(1, 0, 0));
    private Geometry redgeoL, yelgeoL, redgeoR, yelgeoR, whitegeoL, whitegeoR;
    private boolean brakeL = false;
    private boolean brakeR = false;
    private boolean turnL = false;
    private boolean turnR = false;
    private boolean frontLightsOn = false;
    private Vector3f holderPosition = new Vector3f(0f, 0f, 0f);
    private char myColour = 'o';
    private boolean damaged = false;
    private  ParticleEmitter fire;
    private Node textNode1, textNode2, textNode3, textNode4;

    VehicleState(Float x, Float y, Float z, Float h, String name, AssetManager gameAssetMan) {
        Spatial newVehicleNode;
        vehicleName = name;

        Sphere yelp = new Sphere(16, 16, 0.065f);
        Material yel2 = new Material(gameAssetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        yel2.setColor("Color", ColorRGBA.Yellow);
        yel2.setColor("GlowColor", ColorRGBA.Yellow);
        yelgeoL = new Geometry("yelL", yelp);
        yelgeoL.setMaterial(yel2);
        yelgeoL.setLocalTranslation(-1.72f, -0.36f, -0.59f);
        yelgeoL.setLocalScale(0.1f, 1.0f, 1.0f);
        Sphere yelp2 = new Sphere(16, 16, 0.065f);
        yelgeoR = new Geometry("yelR", yelp2);
        yelgeoR.setMaterial(yel2);
        yelgeoR.setLocalTranslation(-1.72f, -0.36f, 0.59f);
        yelgeoR.setLocalScale(0.1f, 1.0f, 1.0f);

        Sphere redp = new Sphere(16, 16, 0.070f);
        Material red2 = new Material(gameAssetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        red2.setColor("Color", ColorRGBA.Red);
        redgeoL = new Geometry("redL", redp);
        redgeoL.setMaterial(red2);
        redgeoL.setLocalTranslation(-1.75f, -0.36f, -0.43f);
        red2.setColor("GlowColor", ColorRGBA.Red);
        redgeoL.setLocalScale(0.2f, 1.0f, 1.0f);
        Sphere redp2 = new Sphere(16, 16, 0.070f);
        redgeoR = new Geometry("redR", redp2);
        redgeoR.setMaterial(red2);
        redgeoR.setLocalTranslation(-1.75f, -0.36f, 0.43f);
        redgeoR.setLocalScale(0.2f, 1.0f, 1.0f);

        Sphere whitep = new Sphere(16, 16, 0.13f);
        Material white2 = new Material(gameAssetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        white2.setColor("Color", ColorRGBA.White);
        whitegeoL = new Geometry("whiteL", whitep);
        whitegeoL.setMaterial(white2);
        whitegeoL.setLocalTranslation(1.58f, -0.55f, -0.48f);
        white2.setColor("GlowColor", ColorRGBA.White);
        whitegeoL.setLocalScale(0.9f, 0.7f, 1.1f);
        Sphere whitep2 = new Sphere(16, 16, 0.13f);
        whitegeoR = new Geometry("whiteR", whitep2);
        whitegeoR.setMaterial(white2);
        whitegeoR.setLocalTranslation(1.58f, -0.55f, 0.48f);
        whitegeoR.setLocalScale(0.9f, 0.7f, 1.1f);

        BitmapFont guiFont = gameAssetMan.loadFont("Interface/Fonts/Default.fnt");
        
        String[] nameAr = name.split("/");
        String shortName=nameAr[4];
        //System.out.println(shortName);
        if (shortName.startsWith("l3") /*|| shortName.equals("centralMember1")*/) //biggest lorry
        {
            newVehicleNode = gameAssetMan.loadModel("Models/truck.j3o");
            newVehicleNode.rotate(PITCH270);
            newVehicleNode.rotate(ROLL180);
            newVehicleNode.setLocalTranslation(5.0f, -1.0f, -4.0f);
            newVehicleNode.setLocalScale(0.01f, 0.01f, 0.01f);
            
            whitegeoR.setLocalTranslation(9.26f, -0.0f, 0.86f);
            whitegeoR.setLocalScale(0.9f, 0.9f, 1.1f);
            whitegeoL.setLocalScale(0.9f, 0.9f, 1.1f);
            whitegeoL.setLocalTranslation(9.26f, -0.0f, -0.38f);
            //frontLightsOn=true;
        }
        else if (shortName.startsWith("d2") || shortName.startsWith("d3"))
        {
            //smaller lorry
            newVehicleNode = gameAssetMan.loadModel("Models/bluelorry.j3o");
            newVehicleNode.rotate(PITCH270);
            newVehicleNode.rotate(ROLL090);
            newVehicleNode.setLocalTranslation(4.5f, -1.0f, 1.5f);
            newVehicleNode.setLocalScale(0.0008f, 0.0008f, 0.0008f);
        }
        
        else if (shortName.startsWith("v2") || shortName.startsWith("v1"))
        {
            newVehicleNode = gameAssetMan.loadModel("Models/Ford.j3o");
            newVehicleNode.rotate(PITCH270);
            newVehicleNode.rotate(ROLL270);
            newVehicleNode.setLocalTranslation(-4.0f, -1.0f, -4.0f);
            newVehicleNode.setLocalScale(0.03f, 0.03f, 0.03f);
        }
        else
        {   
            //car
            newVehicleNode = gameAssetMan.loadModel("Models/Ferrari.j3o");
            newVehicleNode.setLocalRotation(YAW270);
            newVehicleNode.setLocalTranslation(0.0f, -1.0f, 0.0f);
            newVehicleNode.setLocalScale(0.6f, 0.6f, 0.6f);
            newVehicleNode.setName("vehicleVisual");
           /* if (shortName.equals("c1.3"))
            {
                frontLightsOn=true;
            }*/
        }
        
        newVehicleNode.setName("vehicleVisual");
        vehicleNode.attachChild(newVehicleNode);

        Quaternion rotAngle = new Quaternion();
        Float rotRads = h * FastMath.DEG_TO_RAD;
        rotAngle.fromAngles(0, rotRads, 0);
        vehicleNode.setLocalRotation(rotAngle);
        vehicleNode.setLocalTranslation(x, y, z);

        label1 = new BitmapText(guiFont, false);
        label1.setSize(1f);
        label1.setText(info1);
        float textWidth1 = label1.getLineWidth() + info1.length();
        float textOffset1 = textWidth1 / 2;
        label1.setBox(new Rectangle(-textOffset1, 0, textWidth1, label1.getHeight()));
        label1.setColor(new ColorRGBA(255, 0, 0, 1));
        label1.setAlignment(BitmapFont.Align.Center);
        label1.setQueueBucket(RenderQueue.Bucket.Transparent);
        BillboardControl bc1 = new BillboardControl();
        bc1.setAlignment(BillboardControl.Alignment.Screen);
        label1.addControl(bc1);

        label2 = new BitmapText(guiFont, false);
        label2.setSize(1f);
        label2.setText(info2);
        float textWidth2 = label2.getLineWidth() + info2.length();
        float textOffset2 = textWidth2 / 2;
        label2.setBox(new Rectangle(-textOffset2, 0, textWidth2, label2.getHeight()));
        label2.setColor(new ColorRGBA(255, 0, 0, 1));
        label2.setAlignment(BitmapFont.Align.Center);
        label2.setQueueBucket(RenderQueue.Bucket.Transparent);
        BillboardControl bc2 = new BillboardControl();
        bc2.setAlignment(BillboardControl.Alignment.Screen);
        label2.addControl(bc2);

        label3 = new BitmapText(guiFont, false);
        label3.setSize(1f);
        label3.setText(info3);
        float textWidth3 = label3.getLineWidth() + info3.length();
        float textOffset3 = textWidth3 / 2;
        label3.setBox(new Rectangle(-textOffset3, 0, textOffset3, label3.getHeight()));
        label3.setColor(new ColorRGBA(0, 255, 0, 1));
        label3.setAlignment(BitmapFont.Align.Center);
        label3.setQueueBucket(RenderQueue.Bucket.Transparent);
        BillboardControl bc3 = new BillboardControl();
        bc3.setAlignment(BillboardControl.Alignment.Screen);
        label3.addControl(bc3);

        label4 = new BitmapText(guiFont, false);
        label4.setSize(1f);
        label4.setText(info4);
        float textWidth4 = label4.getLineWidth() + info4.length();
        float textOffset4 = textWidth4 / 2;
        label4.setBox(new Rectangle(-textOffset4, 0, textOffset4, label4.getHeight()));
        label4.setColor(new ColorRGBA(0, 0, 255, 1));
        label4.setAlignment(BitmapFont.Align.Center);
        label4.setQueueBucket(RenderQueue.Bucket.Transparent);
        BillboardControl bc4 = new BillboardControl();
        bc4.setAlignment(BillboardControl.Alignment.Screen);
        label4.addControl(bc4);

        textNode1 = new Node("LabelNode1");
        textNode1.setLocalTranslation(0, 3, 0);
        textNode1.attachChild(label1);
        textNode2 = new Node("LabelNode2");
        textNode2.setLocalTranslation(0, 5, 0);
        textNode2.attachChild(label2);
        textNode3 = new Node("LabelNode3");
        textNode3.setLocalTranslation(0, 7, 0);
        textNode3.attachChild(label3);
        textNode4 = new Node("LabelNode4");
        textNode4.setLocalTranslation(0, 9, 0);
        textNode4.attachChild(label4);

        vehicleNode.attachChild(textNode1);
        vehicleNode.attachChild(textNode2);
        vehicleNode.attachChild(textNode3);
        vehicleNode.attachChild(textNode4);

        fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material mat_red = new Material(gameAssetMan, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", gameAssetMan.loadTexture("Textures/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2); 
        fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fire.setStartSize(1.5f);
        fire.setEndSize(0.1f);
        fire.setGravity(0, 0, 0);
        fire.setLowLife(1f);
        fire.setHighLife(3f);
        fire.getParticleInfluencer().setVelocityVariation(0.3f);
    
    }

    public Node getVehicleNode() {
        return vehicleNode;
    }

    public void updateText1(String newTextInfo) {
        info1 = newTextInfo;
    }

    public void updateText2(String newTextInfo) {
        info2 = newTextInfo;
    }
    
    public void updateText3(String newTextInfo) {
        info3 = newTextInfo;
    }
        
    public void updateText4(String newTextInfo) {
        info4 = newTextInfo;
    }
    
    
    public void recreateText()
    {
        label1.setText(info1);
        float textWidth1 = info1.length();
        float textOffset1 = textWidth1 / 2;
        label1.setBox(new Rectangle(-textOffset1, 0, textWidth1, label1.getHeight()));
        
        label2.setText(info2);
        label2.setColor(ColorRGBA.Red);
        float textWidth2 = info2.length();
        float textOffset2 = textWidth2 / 2;
        label2.setBox(new Rectangle(-textOffset2, 0, textWidth2, label2.getHeight()));
        
        label3.setText(info3);
        float textWidth3 = info3.length();
        float textOffset3 = textWidth3 / 2;
        label3.setBox(new Rectangle(-textOffset3, 0, textWidth3, label3.getHeight()));
       // label3.setLocalTranslation(-textOffset3, 0, 0);
        
        label4.setText(info4);
        //label4.setAlignment(BitmapFont.Align.Center);
        float textWidth4 = info4.length();
        float textOffset4 = textWidth4 / 2;
        label4.setBox(new Rectangle(-textOffset4, 0, textWidth4, label4.getHeight()));
    }

    public synchronized void updateValues(Float x, Float y, Float z, Float h) {
        Vector3f newPosition = new Vector3f(x, y, z);
        float distanceMoved = position.distance(newPosition);

        float timeSinceLastUpdate = (System.currentTimeMillis() - lastUpdate);

        float newSpeed = 0;
        if (distanceMoved > 0) {
            newSpeed = distanceMoved / (timeSinceLastUpdate / 1000);
        }

        position = newPosition;
        orientation = (-h * FastMath.DEG_TO_RAD);
        rotAngle = rotAngle.fromAngles(0, orientation, 0);
        //before finally updating the last time we were updated
        lastUpdate = System.currentTimeMillis();
        inferredSpeed = newSpeed;
    }

    public float getOrientation() {
        return orientation;
    }

    public Vector3f getLocation() {
        return position;
    }

    public void updateLightStates(boolean lTurn, boolean rTurn, boolean brakeState, boolean frontState) {
        if (brakeState) {
            brakeL = true;
            brakeR = true;
        } else {
            brakeL = false;
            brakeR = false;
        }
        turnL = lTurn;
        turnR = rTurn;
        frontLightsOn=frontState;
    }
    
    public void updateDamageState(boolean newState) {
        damaged=newState;
    }

    public void updateRenderState() {
        if (!deadReckon) {
            vehicleNode.setLocalRotation(rotAngle);
            vehicleNode.setLocalTranslation(position);
            jasonStateNode.setLocalTranslation(position);
        } else {
            double elapsedSinceUpdate = (System.currentTimeMillis() - lastUpdate);
            float elapsedS = (float) elapsedSinceUpdate / 1000;
            Point2D startPosition = new Point2D(position.x, position.z);
            double distSinceUpdate = elapsedS * inferredSpeed;
            Point2D endPosition = startPosition.createPolar(startPosition, distSinceUpdate, orientation);
            vehicleNode.setLocalRotation(rotAngle);
            vehicleNode.setLocalTranslation(new Vector3f((float) endPosition.getX(), position.getY(), (float) endPosition.getY()));
            jasonStateNode.setLocalTranslation(new Vector3f((float) endPosition.getX(), position.getY(), (float) endPosition.getY()));
        }

        recreateText();
        
        //update light state too
        if (brakeL) {
            vehicleNode.attachChild(redgeoL);
        } else {
            vehicleNode.detachChild(redgeoL);
        }
        if (brakeR) {
            vehicleNode.attachChild(redgeoR);
        } else {
            vehicleNode.detachChild(redgeoR);
        }
        if (turnR) {
            vehicleNode.attachChild(yelgeoR);
        } else {
            vehicleNode.detachChild(yelgeoR);
        }

        if (turnL) {
            vehicleNode.attachChild(yelgeoL);
        } else {
            vehicleNode.detachChild(yelgeoL);
        }

        if (frontLightsOn) {
            vehicleNode.attachChild(whitegeoL);
            vehicleNode.attachChild(whitegeoR);
        } else {
            vehicleNode.detachChild(whitegeoL);
            vehicleNode.detachChild(whitegeoR);
        }  
        
        if (damaged) {
            vehicleNode.attachChild(fire);
        }
        else if (!damaged)
        {
            vehicleNode.detachChild(fire);
        }
    }
}