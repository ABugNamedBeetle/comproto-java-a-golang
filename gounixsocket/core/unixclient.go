package core

type Rectangle struct {
	Length, Width float64
  }


  func (r Rectangle) Area() float64 {
	return r.Length * r.Width
  }
  
  func (r Rectangle) Perimeter() float64 {
	return 2 * (r.Length + r.Width)
  }