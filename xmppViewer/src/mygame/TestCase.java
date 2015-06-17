package mygame;
 
import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
 
//testcase derived from basic jme example

public class TestCase extends SimpleApplication {
 
    public static void main(String[] args){
        TestCase app = new TestCase();
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
 
/*updating centralmember2 geometry: (4747.837, 1.5, 12551.418),  1.5f, 2f, 15.296839
start at (4747.6313, 0.0, 12558.915) end at (4748.0425, 3.0, 12543.921)
and setting localrotation (0.0, -0.99990606, 0.0, 0.013705973)
centralmember2 at (4747.6313, 0.0, 12558.915), checking if collision vol at (4747.837, 1.5, 12551.418) which is 7.6484194m away, and volume contains 4746.3516, 12602.873 which is 43.976635m
centralmember2 contains (4746.3516, 0.0, 12602.873)?: false distance is 51.498363 from centre, nearest edge is 36.15820)

measured in SUMO GUI c2 at 12591.42,4746.14
and                  c1 at 12547.36,4747.81
directly behind c2 is      12629.05,4743.76*/
        
        /*centralmember2 at (4746.8335, 0.0, 12484.009), checking if collision vol at (4747.1777, 1.5, 12491.501) which is 7.648619m away, and volume contains 4748.027, 12509.969 which is 25.987375m
centralmember2 contains (4748.027, 0.0, 12509.969)?: false distance is 18.548037 from centre, nearest edge is 3.1708984
updating centralmember2 geometry: (4746.4897, 1.5, 12476.524),  1.5f, 2f, 15.297238
start at (4746.1455, 0.0, 12469.032) end at (4746.834, 3.0, 12484.017)
and setting localrotation (0.0, -0.022954985, 0.0, 0.9997365*/
        
/*[centralMember1] position updated 4746.145266528288 0 12469.032538490994 267.3686395455154
updating geometry: (4746.834, 1.5, 12484.017),  1.5f, 2f, 30.149992
start at (4746.1455, 0.0, 12469.032) end at (4747.5225, 3.0, 12499.001)
and setting localrotation (0.0, -0.022954985, 0.0, 0.9997365)*/

/*[centralMember2] detected vehicle http://127.0.0.1/vehicles/centralMember1 at 12663.622223671024 4741.206820698129
centralMember2 is at 12735.358 , 4732.1953
centralMember2 startat (4729.823, 0.0, 12751.174), checking if collision vol at (4727.45, 0.0, 12766.996) which is 15.999232m away, and volume contains centralMember1 at 4741.207, 12663.622 which is 88.288734m
centralMember2 vol contains (4741.207, 0.0, 12663.622)?: false distance is 104.28538 from centre, nearest edge is 72.419754
centralMember2updating geometry: (4729.8223, 0.0, 12751.182),  1.5f, 2f, 32.00047)
centralMember2start at (4732.1953, 0.0, 12735.358) end at (4727.4487, 0.0, 12767.005)
centralMember2and setting localrotation (0.0, 0.07437021, 0.0, 0.9972307)*/

/*centralMember2 is at 4475.661 , 11631.869
centralMember2 startat (4479.9233, 0.0, 11647.282), checking if collision vol at (4475.659, 0.0, 11631.861) which is 15.9995985m away, and volume contains centralMember1 at 4475.5596, 11631.502 which is 16.372522m
centralMember2 vol contains (4475.5596, 0.0, 11631.502)?: true distance is 0.37292415 from centre, nearest edge is 0.0
centralMember2updating geometry: (4471.3965, 0.0, 11616.447),  1.5f, 2f, 32.0004
centralMember2start at (4475.661, 0.0, 11631.869) end at (4467.132, 0.0, 11601.026)
centralMember2and setting localrotation (0.0, 0.9909149, 0.0, 0.13449045)
[centralMember2] detected vehicle http://127.0.0.1/vehicles/centralMember1 at 11617.051689274016 4471.563542202977*/

        Box centralMember2 = new Box(1,1,1);
        Geometry car2 = new Geometry("Box", centralMember2);
        car2.setLocalTranslation(new Vector3f(4475.661f, 0f, 11631.869f));
        //car2.setLocalTranslation(new Vector3f(1f, 1.5f, 1f));
        Material mat1 = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Gray);
        car2.setMaterial(mat1);
        rootNode.attachChild(car2);
        
        Box centralMember1 = new Box(1,1,1);
        Geometry car1 = new Geometry("Box", centralMember1);
        car1.setLocalTranslation(new Vector3f(4475.5596f, 0f, 11631.502f));
        Material mat2 = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        car1.setMaterial(mat2);
        rootNode.attachChild(car1);
        
        Box start1 = new Box(0.1f,3f,0.1f);
        Geometry startG1 = new Geometry("Box", start1);
        startG1.setLocalTranslation(new Vector3f(4475.661f, 0.0f, 11631.869f));
        startG1.setMaterial(mat2);
        rootNode.attachChild(startG1);
        Box end1 = new Box(0.1f,3f,0.1f);
        Geometry endG1 = new Geometry("Box", end1);
        endG1.setLocalTranslation(new Vector3f(4467.132f, 0.0f, 11601.026f));
                Material matG = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        matG.setColor("Color", ColorRGBA.Green);
        endG1.setMaterial(matG);
        rootNode.attachChild(endG1);
        
        Vector3f zeroVec = new Vector3f(0f,0f,0f);
        Box collBox1 = new Box(1,1,1);
        Geometry collBoxG1 = new Geometry("Box", collBox1);
        Vector3f geomLoc = new Vector3f(4471.3965f, 0.0f, 11616.447f);
        collBox1.updateGeometry(zeroVec, 1.5f, 2f, 32.0004f);
        Material matBlue = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        matBlue.setColor("Color", ColorRGBA.Blue);
        collBoxG1.setMaterial(matBlue);
        collBoxG1.setLocalRotation(new Quaternion(0.0f, 0.9909149f, 0.0f, 0.13449045f));
        collBoxG1.setLocalTranslation(geomLoc);
        rootNode.attachChild(collBoxG1); 
        
 
        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, car2, inputManager);
        chaseCam.setMaxDistance(100f);
        cam.setFrustumFar(10000);
        chaseCam.setSmoothMotion(true);
       
    }
}
