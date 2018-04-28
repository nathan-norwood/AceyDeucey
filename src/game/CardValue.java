package game;

public enum CardValue {
	TWO
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 2;
		}
	},	THREE
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 3;
		}
	},	FOUR
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 4;
		}
	},	FIVE
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 5;
		}
	},	SIX
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 6;
		}
	},	SEVEN
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 7;
		}
	},	EIGHT
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 8;
		}
	},	NINE
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 9;
		}
	},	TEN
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 10;
		}
	},	JACK
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 11;
		}
	},	QUEEN
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 12;
		}
	},	KING
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 13;
		}
	},	ACE
 {
		@Override
		public int getCardValue() {
			// TODO Auto-generated method stub
			return 14;
		}
 };
		public abstract int getCardValue();
		
}
