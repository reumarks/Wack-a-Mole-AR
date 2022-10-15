import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Wack extends PApplet {



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

public void setup() {
  surface.setTitle("Wack-a-mole");
  
  cameraIndex %= cameras.length;
  video = new Capture(this, cameras[cameraIndex]);
  video.start();
  Font1 = createFont("Arial Bold", 55);
}

public void captureEvent(Capture video) {
  video.read();
}

public void draw() {
  video.loadPixels();
  
  if(stage == "Setup"){
    if(holes.size() < 4){
      mouse = CROSS;
    }else{
      mouse = ARROW;
    }
    pushMatrix();
    scale(-1.0f, 1.0f);
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
    
    if(mouseIsReleased && mouse == CROSS && percentMatch((int)(mouseX - size), (int)(mouseY - size), (int)(mouseX + size), (int)(mouseY + size), 2, video.pixels[(int)(mouseX + mouseY * video.width)], 80) > 0.7f){
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
      h.displayGame(width*((float)i+0.50f)/(float)holes.size(), 300 + sin(0.5f*3.1415926535f+(float)i*3.1415926535f)*40, (4.0f/(float)holes.size()));
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


public boolean button(float x, float y, float w, float h, String txt){
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


public float percentMatch(int minX, int minY, int maxX, int maxY, int step, int matchColor, float matchThreshold){
  float total = 0;
  float match = 0;
  for (int x = (((minX) > 0)?(minX):0); (x < video.width && x < maxX); x+= step) {
    for (int y = (((minY) > 0)?(minY):0); (y < video.height && y < maxY); y+= step) {
      loc = x + y * video.width;
      int currentColor = video.pixels[loc];
      total ++;
      if (distSq(red(currentColor), green(currentColor), blue(currentColor), red(matchColor), green(matchColor), blue(matchColor)) < matchThreshold*matchThreshold) {
        match ++;
      }
    }
  }
  return (match/total);
}

public float distSq(float x1, float y1, float z1, float x2, float y2, float z2) {
  float d = (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) +(z2-z1)*(z2-z1);
  return d;
}

public void mouseReleased(){
  mouseIsReleased = true;
}
class Hole {
  float posX;
  float posY;
  float target = 0;
  float out = 0;
  int timer = 100;
  int holeColor;
  float matchPercent;
  int move = 0;
  float bonkTime = 0;
  float headSpin = 0;
  boolean dir = true;
  float bonkPlace = 0;
  boolean hit = false;
  
  Hole(float x, float y) {
    posX = video.width - x;
    posY = y;
    holeColor = video.pixels[(int)(video.width - x + y * video.width)]; 
    headSpin = 0;
    target = 0;
    out = 0;
    timer = 100;
    move = 0;
    bonkTime = 0;
    
    matchPercent = percentMatch((int)(posX - size), (int)(posY - size), (int)(posX + size), (int)(posY + size), 2, holeColor, 80);
  }

  public void displayDebug() {
    if(IsVisible()){
      stroke(0);
    }else{
      stroke(255, 0, 0);
    }
    fill(holeColor);
    strokeWeight(2);
    rect(video.width - (posX + size), posY - size, size*2, size*2);
    
    fill(0);
    textSize(10);
    text(matchPercent, video.width - posX, posY - 5); 
    float vp = percentMatch((int)(posX - size), (int)(posY - size), (int)(posX + size), (int)(posY + size), 2, holeColor, 80);
    text(vp, video.width - posX, posY + 5);
  }
  
  public boolean IsVisible(){
    float visiblePercent = percentMatch((int)(posX - size), (int)(posY - size), (int)(posX + size), (int)(posY + size), 5, holeColor, 80);
    return visiblePercent > matchPercent * 0.7f;
  }
  
  public boolean mouseOver(){
    return mouseX > video.width - (posX + size) && mouseY > posY - size && mouseX < video.width - (posX - size) && mouseY < posY + size;
  }
  
  public void update(){
    if(bonkTime > 0){
      bonkTime --;
      out = lerp(out, -20, 0.4f);
      headSpin = bonkTime*2*3.1415926535f/30*(dir?-1:1);
      while(headSpin > 3.1415926535f){
        headSpin -= 2*3.1415926535f;
      }
    }else{
      if(!IsVisible()){
        if(out > 5 && move != 2){
          bonkTime = 30;
          score++;
          move = 0;
          target = 0;
          timer = 60;
          dir = !dir;
          bonkPlace = out;
          hit = true;
        }else{
          hit = false;
          bonkTime = 60;
          score --;
        }
      }
    
      if(timer == 0){
        headSpin = lerp(headSpin, 0, 0.2f);
        if(move != 1){
          move = (int)floor(random(0, 4.99f));
        }else{
          move = 0;
        }
        if(move == 1){
          if(IsVisible()){
            target = 100;
          }else{
            target = 20;
          }
          timer = 25;
        }else if(move == 2){
          target = 20;
          timer = 40;
        }else{
          target = -20;
          timer = 40;
        }
      }
      timer --;
      out = lerp(out, target, 0.2f);
    }
  }
  

  public void displayGame(float x, float y, float size){
      pushMatrix();
        translate(x, y);
        scale(size);
        translate(-x, -y);
        noStroke();
        fill(79, 79, 79);
        ellipse(x, y, 90, 40);
        fill(51, 51, 51);
        ellipse(x, y + 5, 74, 30);
        noFill();
        strokeWeight(20);
        stroke(255, 172, 89);
        arc(x, y, 100, 50, radians(180), radians(360));
        stroke(43, 43, 43);
        strokeWeight(3);
        fill(255, 138, 36);
        rect(x - 35, y - out + 55, 70, 35 + out);
        noStroke();
        pushMatrix();
          translate(x, y - out + 55);
          rotate(headSpin);
          stroke(43, 43, 43);
          strokeWeight(6);
          ellipse(23, -24, 29, 29);
          ellipse(-23, -24, 29, 29);
          arc(0, 0, 70, 70, radians(167)-headSpin, radians(367)-headSpin);
          noStroke();
          fill(255, 111, 0);
          ellipse(23, -24, 29, 29);
          ellipse(-23, -24, 29, 29);
          fill(255, 138, 36);
          ellipse(23, -24, 20, 20);
          ellipse(-23, -24, 20, 20);
          ellipse(0, 0, 70, 70);
          noFill();
          stroke(43, 43, 43);
          strokeWeight(8);
          point(-10, 3);
          point(10, 3);
          strokeWeight(3);
          fill(255, 111, 0);
          triangle(0, 31, 8, 15, - 8, 15);
        popMatrix();
        noFill();
        strokeWeight(20);
        stroke(255, 172, 89);
        arc(x, y, 100, 50, 0, radians(180));
        noStroke();
        fill(255, 172, 89);
        rect(x - 60, y + 16, 120, 200);
        
        if(hit){
        fill(255, 255, 255, 100 + (bonkTime - 30) * 10);
        noStroke();
        ellipse(x, y - bonkPlace + 40, 100 + (30 - bonkTime) * 5, 100 + (30 - bonkTime) * 5);
        textFont(Font1);
        textAlign(CENTER, CENTER);
        fill(255, 255, 255, 100 + (bonkTime - 30) * 5);
        text("+1", x, y - bonkPlace + 10 - (30 - bonkTime) * 2);
        }else{
          fill(255, 0, 0, 100 + (bonkTime - 60) * 10);
          noStroke();
          ellipse(x, y - bonkPlace + 40, 100 + (60 - bonkTime) * 5, 100 + (60 - bonkTime) * 5);
          textFont(Font1);
          textAlign(CENTER, CENTER);
          fill(255, 0, 0, 100 + (bonkTime - 60) * 5);
          text("-1", x, y - bonkPlace + 10 - (60 - bonkTime) * 2);
        }
      popMatrix();
  }
}
  public void settings() {  size(640, 480); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Wack" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
