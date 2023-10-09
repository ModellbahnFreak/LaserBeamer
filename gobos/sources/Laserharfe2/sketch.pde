ArrayList<Punkt> pt= new ArrayList<Punkt>();
void setup (){
  fullScreen ();
  ellipseMode (CENTER);
  //frameRate (2);
  fillList ();
}
float b = 0;
float speed = 0.01;
void draw (){
  background (0);
  for (Punkt p:pt) {
    p.setXFact(b);
    p.display();
  }
  
  b+=speed;
  if (b>=3 || b <= 0){
    speed =-speed;
  }
}

void fillList () {
  pt.clear ();
  for (int i=0; i < 10; i++) {
    //println ((width/2.0)-((i/5.0)*width/2.0));
    pt.add (new Punkt ((width/2.0), height/2, -i));
    pt.add (new Punkt ((width/2.0), height/2, i));
  }
}

class Punkt {
  float x,y,s;
  Punkt (float _x, float _y, float _s){
    x=_x;
    y=_y;
    s=_s;
  }
  void display() {
    fill (255);
    noStroke ();
    ellipse (x, y, 10, 10);
  }
  void setXFact (float t) {
    x=(width/2)+(t*(s/10)*width/2);
  }
}