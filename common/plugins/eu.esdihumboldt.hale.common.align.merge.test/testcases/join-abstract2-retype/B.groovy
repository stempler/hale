schema('B') {
	B1 {
		b1()
	}
	
	B2 {
		b2()
		b1(cardinality: '?')
		b3()
	}
	
	B3 {
		b3()
	}
}