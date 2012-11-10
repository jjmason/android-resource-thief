package jm.util;
public final class Tuple {
	private Tuple(){}
	private static abstract class TupleBase {
		private Object[] mElements;
		private Integer  mHashCode;
		private String   mString;
		
		protected abstract Object[] elements(); 
		protected abstract int count();
		
		private Object[] ensureElements(){
			if(mElements == null)
				mElements = elements();
			return mElements;
		}
		
		@Override
		public final int hashCode() {
			if(mHashCode == null){
				int v = 0x345678;
				Object[] elements = ensureElements();
				for(Object o : elements){
					int h = o == null ? 0x7654321 : o.hashCode();
					v = (v * 1000003) ^ h;
				}
				mHashCode = v ^ elements.length; 
			}
			return mHashCode;
		}
		
		@Override
		public final boolean equals(Object other){
			if(other == null || other.getClass() != getClass())
				return false;
			TupleBase tuple = (TupleBase) other;
			if(hashCode() != tuple.hashCode())
				return false;
			if(count() != tuple.count())
				return false;
			Object[] a = ensureElements();
			Object[] b = tuple.ensureElements();
			for(int i=0; i < a.length; i++){
				Object oa = a[i];
				Object ob = b[i];
				if(oa != ob && (oa == null || !oa.equals(ob)))
					return false;
			}
			return true;
		}
	
		@Override
		public String toString(){
			if(mString == null){
				StringBuilder sb = new StringBuilder().append("(");
				Object[] elements = ensureElements();
				for(int i=0;i<elements.length;i++){
					if(i != 0){
						sb.append(",");
					}
					sb.append(elements[i]);
				}
				mString = sb.append(")").toString();
			}
			return mString;
		}
		 
	}
	
	public static class Tuple2<E1,E2> extends TupleBase {
		public final E1 first;
		public final E2 second;
		public Tuple2(E1 e1, E2 e2){
			this.first = e1;
			this.second = e2;
		}
		@Override
		protected Object[] elements() {
			return new Object[] { first, second };
		}
		@Override
		protected int count() {
			return 2;
		}
	}
	
	public static class Tuple3<E1, E2, E3> extends TupleBase {
		public final E1 first; 
		public final E2 second;
		public final E3 third;
		public Tuple3(E1 first, E2 second, E3 third){
			this.first = first;
			this.second = second;
			this.third = third;
		}
		@Override
		protected Object[] elements() {
			return new Object[]{first,second,third};
		}
		@Override
		protected int count() {
			return 3;
		}
	}
	
	
	public static class Tuple4<E1, E2, E3, E4> extends TupleBase {
		public final E1 first; 
		public final E2 second;
		public final E3 third;
		public final E4 fourth;
		public Tuple4(E1 first, E2 second, E3 third, E4 fourth){
			this.first = first;
			this.second = second;
			this.third = third;
			this.fourth = fourth;
		}
		@Override
		protected Object[] elements() {
			return new Object[]{first,second,third, fourth};
		}
		@Override
		protected int count() {
			return 4;
		}
	}
	
	
	public static class Tuple5<E1, E2, E3, E4,E5> extends TupleBase {
		public final E1 e1; 
		public final E2 e2;
		public final E3 e3;
		public final E4 e4;
		public final E5 e5;
		
		public Tuple5(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5){
			this.e1 = e1;
			this.e2 = e2;
			this.e3 = e3;
			this.e4 = e4;
			this.e5 = e5;
		}
		@Override
		protected Object[] elements() {
			return new Object[]{e1,e2,e3,e4,e5};
		}
		@Override
		protected int count() {
			return 3;
		}
	}
}
