import processing.video.*;

Capture video;
int loc; 
boolean db = true;
String stage = "Setup";
boolean mouseIsReleased = false;
float size = 15;
int mouse = ARROW;
String[] cameras = Capture.list();
int cameraIndex = 1;
PFont Font1;
int score = 0;
float time = 6000;
int highScore = 0;

ArrayList<Hole> holes = new ArrayList<Hole>();

void setup() {
  surface.setTitle("Wack-a-mole");
  size(640, 480);
  cameraIndex %= cameras.length;
  video = new Capture(this, cameras[cameraIndex]);
  video.start();
  Font1 = createFont("Arial Bold", 55);
}

void captureEvent(Capture video) {
  video.read();
}

void draw() {
  video.loadPixels();
  
  if(stage == "Setup"){
    if(holes.size() < 4){
      mouse = CROSS;
    }else{
      mouse = ARROW;
    }
    pushMatrix();
    scale(-1.0, 1.0);
    image(video, -video.width, 0);
    popMatrix();
    
    for(int i = 0; i < holes.size(); i++){
      Hole h = holes.get(i);
      h.displayDebug();
      if(h.mouseOver()){
        mouse = HAND;
        if(mouseIsReleased){
          holes.remove(i);
        }
      }
    }
    textFont(Font1);
    textAlign(CENTER, CENTER);
    fill(240, 153, 67);
    textSize(30);
    text("High Score: " + highScore, width/2 - 25, 20);
    
    if(button(width/2-137/2, 45, 137, 45, "Play") && holes.size() == 4){
      stage = "Play";  
    }
    
    if(holes.size() != 4){
      fill(155, 103, 48, 80);
      rect(width/2-137/2, 45, 137, 45);
    }
    
    if(button(200, 45, 45, 45, "#")){
      video.stop();
      cameraIndex ++;
      cameraIndex %= cameras.length;
      video = new Capture(this, cameras[cameraIndex]);
      video.start();
    }
    
    if(mouseIsReleased && mouse == CROSS && percentMatch((int)(mouseX - size), (int)(mouseY - size), (int)(mouseX + size), (int)(mouseY + size), 2, video.pixels[(int)(mouseX + mouseY * video.width)], 80) > 0.7){
      holes.add(new Hole(mouseX, mouseY));
    }
    
    if(mouse == CROSS){
      stroke(70);
      strokeWeight(1);
      fill(video.pixels[(int)(video.width - mouseX + mouseY * video.width)]);
      ellipse(mouseX - 10, mouseY - 10, 10, 10);
    }
    
  }
  if(stage == "Play"){
    mouse = -1;
    background(255, 172, 89);
    textFont(Font1);
    textAlign(CENTER, CENTER);
    fill(240, 153, 67);
    text("Score: " + score, width/2, 100);
    
    for(int i = 0; i < holes.size(); i++){
      Hole h = holes.get(i);
      h.displayGame(width*((float)i+0.50)/(float)holes.size(), 300 + sin(0.5*3.1415926535+(float)i*3.1415926535)*40, (4.0/(float)holes.size()));
      h.update();
    }
    time --;
    fill(240, 153, 67);
    noStroke();
    rect(0, height - 40, width * time/6000, 40);
    if(time == 0){
      stage = "Setup";
      time = 6000;
      for(int i = 0; i < holes.size(); i++){
        Hole h = holes.get(i);
        h.headSpin = 0;
        h.target = 0;
        h.out = 0;
        h.timer = 100;
        h.move = 0;
        h.bonkTime = 0;
      }      
      score = 0;
      if(score > highScore){
        highScore = score;
      }
    }
  }
  
  if(mouse != -1){
    cursor(mouse);
  }else{
    noCursor();
  }
  mouseIsReleased = false;
}


boolean button(float x, float y, float w, float h, String txt){
    boolean output = false;
    textFont(Font1);
    strokeWeight(3);
    stroke(240, 153, 67);
    fill(255, 203, 148);
    rect(x, y, w, h);
    fill(220, 133, 57);
    textSize(20);
    textAlign(CENTER, CENTER);
    text(txt, x + w/2, y + h/2);
    if(mouseX > x && mouseY > y && mouseX < x + w && mouseY < y + h){
      mouse = HAND;
      if(mouseIsReleased){
        output = true;
      }
    }
    return output;
};


float percentMatch(int minX, int minY, int maxX, int maxY, int step, color matchColor, float matchThreshold){
  float total = 0;
  float match = 0;
  for (int x = (((minX) > 0)?(minX):0); (x < video.width && x < maxX); x+= step) {
    for (int y = (((minY) > 0)?(minY):0); (y < video.height && y < maxY); y+= step) {
      loc = x + y * video.width;
      color currentColor = video.pixels[loc];
      total ++;
      if (distSq(red(currentColor), green(currentColor), blue(currentColor), red(matchColor), green(matchColor), blue(matchColor)) < matchThreshold*matchThreshold) {
        match ++;
      }
    }
  }
  return (match/total);
}

float distSq(float x1, float y1, float z1, float x2, float y2, float z2) {
  float d = (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) +(z2-z1)*(z2-z1);
  return d;
}

void mouseReleased(){
  mouseIsReleased = true;
}
