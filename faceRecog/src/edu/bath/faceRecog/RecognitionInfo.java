package edu.bath.faceRecog;

  class RecognitionInfo
  {
        private String suspectedName = "";
	private float confidence = 0.0f;
      
       	RecognitionInfo(String name, float conf)
        {
		suspectedName=name;
		confidence=conf;
        }
        
        public String getName()
        {
            return suspectedName;
        }

        public float getConfidence()
        {
            return confidence;
        }
  }
