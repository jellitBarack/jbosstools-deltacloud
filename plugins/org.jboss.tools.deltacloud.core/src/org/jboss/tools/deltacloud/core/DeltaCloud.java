package org.jboss.tools.deltacloud.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.jboss.tools.deltacloud.core.client.DeltaCloudClient;
import org.jboss.tools.deltacloud.core.client.DeltaCloudClientException;
import org.jboss.tools.deltacloud.core.client.Image;
import org.jboss.tools.deltacloud.core.client.Instance;

public class DeltaCloud {
	
	private String name;
	private String username;
	private URL url;
	private DeltaCloudClient client;
	
	
	ListenerList instanceListeners = new ListenerList();
	ListenerList imageListeners = new ListenerList();
	
	public DeltaCloud(String name, URL url, String username, String passwd) throws MalformedURLException {
		this.client = new DeltaCloudClient(url, username, passwd);
		this.url = url;
		this.name = name;
		this.username = username;
	}

	public String getName() {
		return name;
	}
	
	public String getURL() {
		return url.toString();
	}
	
	public String getUsername() {
		return username;
	}
	
	public void addInstanceListListener(IInstanceListListener listener) {
		instanceListeners.add(listener);
	}
	
	public void removeInstanceListListener(IInstanceListListener listener) {
		instanceListeners.remove(listener);
	}

	public void notifyInstanceListListeners(DeltaCloudInstance[] array) {
		Object[] listeners = instanceListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((IInstanceListListener)listeners[i]).listChanged(array);
	}
	
	public void addImageListListener(IImageListListener listener) {
		imageListeners.add(listener);
	}
	
	public void removeImageListListener(IImageListListener listener) {
		imageListeners.remove(listener);
	}
	
	public void notifyImageListListeners(DeltaCloudImage[] array) {
		Object[] listeners = imageListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((IImageListListener)listeners[i]).listChanged(array);
	}

	public DeltaCloudInstance[] getInstances() {
		ArrayList<DeltaCloudInstance> instances = new ArrayList<DeltaCloudInstance>();
		try {
			List<Instance> list = client.listInstances();
			for (Iterator<Instance> i = list.iterator(); i.hasNext();) {
				DeltaCloudInstance instance = new DeltaCloudInstance(i.next());
				instances.add(instance);
			}
		} catch (DeltaCloudClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
		instanceArray = instances.toArray(instanceArray);
		notifyInstanceListListeners(instanceArray);
		return instanceArray;
	}
	
	public DeltaCloudImage[] getImages() {
		ArrayList<DeltaCloudImage> images = new ArrayList<DeltaCloudImage>();
		try {
			List<Image> list = client.listImages();
			for (Iterator<Image> i = list.iterator(); i.hasNext();) {
				DeltaCloudImage image = new DeltaCloudImage(i.next());
				images.add(image);
			}
		} catch (DeltaCloudClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return images.toArray(new DeltaCloudImage[images.size()]);
	}

}