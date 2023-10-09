ArrayList<Punkt> pt= new ArrayList<Punkt>();
void setup (){
  fullScreen ();
  ellipseMode (CENTER);
  frameRate (7);
  fillList ();
}
float w = 0.1;
int anzPt = 10;
int step = anzPt-1;
void draw (){
  background (0);
  pt.get (step).setX (w);
  for (Punkt p:pt) {
    p.display();
  }
  step--;
  if (step < 0){
    step =anzPt-1;
  }
}

void fillList () {
  pt.clear ();
  for (int i=0; i < anzPt; i++) {
    //println ((width/2.0)-((i/5.0)*width/2.0));
    pt.add (new Punkt ((float)i/anzPt*0.9-1, height/2));
    //pt.add (new Punkt ((width/2.0), height/2, i));
  }
}

class Punkt {
  float x,y;
  Punkt (float _x, float _y){
    x=_x;
    y=_y;
  }
  void display() {
    fill (255);
    noStroke ();
    ellipse (x*width, y, 10, 10);
  }
  void setX (float t) {
    x+=t;
  }
}