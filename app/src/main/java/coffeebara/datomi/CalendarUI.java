package coffeebara.datomi;

import android.widget.*;
import android.view.*;
import android.content.Context;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class CalendarUI extends LinearLayout {
	SimpleMode simpleMode;
	DetailMode detailMode;
	Submit submit;
	private ViewGroup.MarginLayoutParams matchParentWidth = new ViewGroup.MarginLayoutParams(              // ViewGroup.MarginLayoutParams extends from ViewGroup.LayoutParams
									ViewGroup.LayoutParams.MATCH_PARENT,
									ViewGroup.LayoutParams.WRAP_CONTENT );
	private ViewGroup.MarginLayoutParams wrapContent = new ViewGroup.MarginLayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );

	Calendar today = Calendar.getInstance();
	Calendar selectedDate = Calendar.getInstance();
	int selectedDay = today.get( Calendar.DAY_OF_MONTH );
	int selectedMonth = today.get( Calendar.MONTH );
	int selectedYear = today.get( Calendar.YEAR );

	CalendarUI ( Context context, OnSubmit onSubmit ) {
		super( context );
		simpleMode = new SimpleMode( context );
		detailMode = new DetailMode( context );
		submit = new Submit( context, onSubmit);
		setOrientation( LinearLayout.VERTICAL );
		setLayoutParams( matchParentWidth );
		setGravity( Gravity.CENTER );
		setPadding( 13, 13, 13, 13 );

		addView( detailMode );
		addView( submit );
	}

	public interface OnSubmit {
			public void onDone( Calendar date );
			public void onCancel();
	}

	class Submit extends GridLayout {
		Button btnCancel;
		Button btnDone;
		Submit( Context context, OnSubmit onSubmit ) {
			super(context);
			setLayoutParams( matchParentWidth );
			setPadding(0, 17, 0 , 0);
			setColumnCount( 2 );
			setRowCount( 1 );
			btnCancel = new Button( context );
			btnCancel.setGravity( Gravity.CENTER );
			btnDone   = new Button( context );
			btnDone.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					onSubmit.onDone( detailMode.getSelectedDate() );
				}
			} );
			btnCancel.setText("Cancel");
			btnCancel.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( View view ) {
					(( ViewGroup )CalendarUI.this.getParent()).removeView( CalendarUI.this );
					onSubmit.onCancel();
				}
			});
			btnDone.setText("Done");
			
			LinearLayout cancelWrapper = new LinearLayout( context );
			cancelWrapper.setGravity( Gravity.CENTER ); 			
			cancelWrapper.addView( btnCancel ); 
			
			LinearLayout doneWrapper = new LinearLayout( context );             
			doneWrapper.setGravity( Gravity.CENTER ); 
			doneWrapper.addView( btnDone );

			addView(cancelWrapper, new GridLayout.LayoutParams( GridLayout.spec( 0 , 1f ), GridLayout.spec( 0, 1f )) );
			addView(doneWrapper  , new GridLayout.LayoutParams( GridLayout.spec( 0 , 1f ), GridLayout.spec( 1, 1f )) );
		}
	}
	class SimpleMode extends LinearLayout {
		TextView selectedDate;
		SimpleMode ( Context context ) {
			super( context );
			selectedDate = new TextView( context );
		}
		
		
	}
	class DetailMode extends LinearLayout {
		Header header;
		Almanac almanac;

		DetailMode ( Context context ) {
			super( context );
			header = new Header( context );
			almanac = new Almanac( context );
			setOrientation( LinearLayout.VERTICAL );
			addView( header );
			addView( almanac );
			
		}
		public Calendar getSelectedDate() {
			return almanac.getSelectedDate();
		}
		private class Header extends RelativeLayout {
			final int NEXT = 1;
			final int PREV = 0;
			TextView month;
			TextView year;
			Button prev;
			Button next;
			Calendar focusedDate = Calendar.getInstance();

			Header( Context context ) {
				super( context );
				month = new TextView( context );
				ViewGroup.MarginLayoutParams marginRight = new ViewGroup.MarginLayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT  );
				marginRight.setMargins( 0, 0, 30, 0);
				month.setLayoutParams( marginRight );
				year = new TextView( context );

				LinearLayout dateWrapper = new LinearLayout( context );
				dateWrapper.addView( month );
				dateWrapper.addView( year );

				View[] views = new View[] {
					prev = new Button( context ),
					next = new Button( context ),
					dateWrapper
				};

				prev.setText( "<" );
				next.setText( ">" );
				prev.setLayoutParams( layout( RelativeLayout.ALIGN_PARENT_LEFT ) );
				next.setLayoutParams( layout( RelativeLayout.ALIGN_PARENT_RIGHT ) );
				prev.setOnClickListener( explore( PREV ) );
				next.setOnClickListener( explore( NEXT ));

				dateWrapper.setLayoutParams( layout( RelativeLayout.CENTER_IN_PARENT ) );

				for ( View view : views ) {
					addView( view );
				}
				setFocusedDate( focusedDate );
				setLayoutParams( matchParentWidth );
				display();
			}

			public void setFocusedDate( Calendar date ) {
				focusedDate = date;
			}
			public void display() {
				month.setText( new SimpleDateFormat("MMMM").format( focusedDate.getTimeInMillis() ));
				year.setText( new SimpleDateFormat("yyyy").format( focusedDate.getTimeInMillis() ));
			}
			public Calendar getFocusedDate() {
				return focusedDate;
			}

			public View.OnClickListener explore( int direction ) {
				return new View.OnClickListener() {
					@Override
					public void onClick( View view ) {
						int step = 0;
						switch( direction ) {
							case NEXT:
									step = 1;
								break;
							case PREV:
									step = -1;
								break;
						}
						getFocusedDate().add( Calendar.MONTH, step );
						display();
						almanac.setFocusedDate( focusedDate );
					}
				};
			}
			private RelativeLayout.LayoutParams layout( int rule ) {
				RelativeLayout.LayoutParams wrapContent = new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
				wrapContent.addRule( rule );
				return wrapContent;
			}
		}
		private class Almanac extends GridLayout {
		
			final float WEIGHT = 1f;
			final int COLUMN = 7;
			final int ROW = 7;
			Calendar originalDate = Calendar.getInstance();
			Calendar selectedDate = Calendar.getInstance();
			TextView prevDay;
			Almanac ( Context context ) {
				super( context );
				prevDay = new TextView( context );
				int count = 0;
				setColumnCount( COLUMN );
				setRowCount( ROW );
				
				setUseDefaultMargins(true); //
				setLayoutParams( matchParentWidth );
				setGravity( Gravity.CENTER );
				{
					Calendar cal = Calendar.getInstance();
					for ( int i = 0; i < COLUMN; i++ ) {
						cal.set( Calendar.DAY_OF_WEEK, i + 1 );
						GridLayout.LayoutParams params = new GridLayout.LayoutParams( GridLayout.spec( 0, WEIGHT ), GridLayout.spec( i, WEIGHT ) );
						TextView weekName = new TextView( context );
						weekName.setText( new SimpleDateFormat("EE").format( cal.getTimeInMillis() ) );
						weekName.setGravity( Gravity.CENTER );
						addView( weekName, params );
					}
				}
				for ( int x = 1; x < ROW; x++ ) {
					for ( int y = 0; y < COLUMN; y++ ) {
						GridLayout.LayoutParams params = new GridLayout.LayoutParams( GridLayout.spec( x, WEIGHT ), GridLayout.spec( y, WEIGHT ) );
						TextView day = new TextView( context );
						day.setOnClickListener( new View.OnClickListener() {
							@Override
							public void onClick( View view ) {
								if ( day.getText() != "" ) {
									prevDay.setBackground( null );
									prevDay = day;
									selectedDate.set( Calendar.DAY_OF_MONTH, Integer.parseInt( day.getText().toString() ) );
									day.setBackgroundColor( Color.parseColor( "#aaaa00" ) );
								}
							}
						});
						day.setGravity( Gravity.CENTER );
						addView( day, params );
					}
				}

				setFocusedDate( Calendar.getInstance() );


			}
			Calendar getSelectedDate() {
				return selectedDate;
			}
			void setFocusedDate( Calendar date ) {
				for ( int i = 7; i < getChildCount(); i ++ ) {
					(( TextView )getChildAt( i )).setText( "" );
				}
				selectedDate.setTimeInMillis( date.getTimeInMillis() );
				selectedDate.set( Calendar.DAY_OF_MONTH, 1 );
				
				Calendar dateMap = Calendar.getInstance();
				dateMap.setTimeInMillis( selectedDate.getTimeInMillis() );
				int currentMonth = dateMap.get( Calendar.MONTH );
				int firstDayWeekDayName = dateMap.get( Calendar.DAY_OF_WEEK );

				while ( dateMap.get( Calendar.MONTH ) == currentMonth ){
					int day = dateMap.get( Calendar.DAY_OF_MONTH );
					TextView tv = ( TextView ) getChildAt( day + ( COLUMN - 1 ) + firstDayWeekDayName - 1 );
					tv.setGravity( Gravity.CENTER );
					tv.setText( String.valueOf( day ) );
					dateMap.add( Calendar.DAY_OF_MONTH, 1 );
				}
			}
		} // Almanac

	}
}

// onDateSelected()
