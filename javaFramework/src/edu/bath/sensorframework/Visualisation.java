package edu.bath.sensorframework;

/**
 * Interface which visualisations should implement. Only experimental for now.
 * @author adan
 *
 * @param <T> Type of data it handles.
 */
public interface Visualisation<T> {
	/**
	 * Fetch the underlying widget.
	 * @return Widget
	 */
	public Object getWidget();
	
	/**
	 * Adds a data item to the visualisation.
	 * @param data Data to add.
	 */
	public void addData(T data);
	
	/**
	 * Removes all data from visualisation and restores it to its 
	 * default state.
	 */
	public void clear();
	
	/**
	 * Updates visualisation.
	 */
	public void update();
}
