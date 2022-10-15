class Hole {
  float posX;
  float posY;
  float target = 0;
  float out = 0;
  int timer = 100;
  color holeColor;
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

  void displayDebug() {
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
  
  boolean IsVisible(){
    float visiblePercent = percentMatch((int)(posX - size), (int)(posY - size), (int)(posX + size), (int)(posY + size), 5, holeColor, 80);
    return visiblePercent > matchPercent * 0.7;
  }
  
  boolean mouseOver(){
    return mouseX > video.width - (posX + size) && mouseY > posY - size && mouseX < video.width - (posX - size) && mouseY < posY + size;
  }
  
  void update(){
    if(bonkTime > 0){
      bonkTime --;
      out = lerp(out, -20, 0.4);
      headSpin = bonkTime*2*3.1415926535/30*(dir?-1:1);
      while(headSpin > 3.1415926535){
        headSpin -= 2*3.1415926535;
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
        headSpin = lerp(headSpin, 0, 0.2);
        if(move != 1){
          move = (int)floor(random(0, 4.99));
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
      out = lerp(out, target, 0.2);
    }
  }
  

  void displayGame(float x, float y, float size){
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
