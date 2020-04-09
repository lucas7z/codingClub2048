class Tile {
  int value;
  PVector position;
  PVector pixelPos;//top left
  boolean alreadyIncreased = false;
  boolean moving = false;

  color colour;
  PVector positionTo;
  PVector pixelPosTo;
  HashMap<Integer, int[]> map;

  boolean deathOnImpact =false;

  Tile(int x, int y) {
    map = new HashMap();
    if (random(1)< 0.1) {
      value = 4;
    } else {
      value =2;
    }

    position = new PVector(x, y);
    positionTo = new PVector(x, y);

    pixelPos = new PVector(xoffset +x*200 + (x+1) *10, yoffset + y*200 + (y+1) *10);
    pixelPosTo = new PVector(xoffset + x*200 + (x+1) *10, yoffset + y*200 + (y+1) *10);
    setAllColor();
    setColour();
  }
  
  

  void show() {
    if (!deathOnImpact || moving) {
      fill(colour);
      noStroke();
      rect(pixelPos.x, pixelPos.y, 200, 200);
      if (value < 2048) {
        fill(40);
      } else {
        fill(240);
      }
      textAlign(CENTER, CENTER);
      textSize(50);
      text(value, pixelPos.x+100, pixelPos.y+100);
    }
  }

  void moveTo(PVector to) {
    positionTo = new PVector(to.x, to.y);
    pixelPosTo = new PVector(xoffset +to.x*200 + (to.x+1) *10, yoffset +to.y*200 + (to.y+1) *10);
    moving = true;
  }
  void moveToNow(PVector to) {
    position = new PVector(to.x, to.y);
    pixelPos = new PVector(xoffset +to.x*200 + (to.x+1) *10, yoffset +to.y*200 + (to.y+1) *10);
  }

  void move(int speed) {
    if (moving) {
      if (!teleport && dist(pixelPos.x, pixelPos.y, pixelPosTo.x, pixelPosTo.y) > speed) {
        PVector MoveDirection = new PVector(positionTo.x - position.x, positionTo.y - position.y); 
        MoveDirection.normalize();
        MoveDirection.mult(speed);
        pixelPos.add(MoveDirection);
      } else {
        moving = false;
        pixelPos = new PVector(pixelPosTo.x, pixelPosTo.y);
        position = new PVector(positionTo.x, positionTo.y);
      }
    }
  }
  
  void setAllColor(){
    map.put(2,new int[] {0, 255, 80});
    map.put(4,new int[] {0, 255, 90});
    map.put(32,new int[] {0, 255, 100});
    map.put(64,new int[] {0, 255, 110});
    map.put(128,new int[] {10, 0, 120});
    map.put(256,new int[] {100, 0, 0});
    map.put(512,new int[] {150, 0, 0});
    map.put(1024,new int[] {0, 0, 10});
    map.put(2048,new int[] {0, 0, 20});
    map.put(4096,new int[] {0, 0, 40});
    map.put(8192,new int[] {0, 0, 80});
  }
  
  void setColour() {
    try{
    int[] val = map.get(value);
    this.colour = color(val[0],val[1],val[2]);
    }catch(NullPointerException e){
      int[] val = map.get(2);
      this.colour = color(val[0],val[1],val[2]);
    }
  }

  Tile clone() {
    Tile clone = new Tile(floor(position.x), floor(position.y));
    clone.value = value;
    clone.setColour();

    return clone;
  }
}
