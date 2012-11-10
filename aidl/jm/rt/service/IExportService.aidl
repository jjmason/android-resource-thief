package jm.rt.service;
import jm.rt.service.ExportStatus;
import jm.rt.service.ResourceGID;
import jm.rt.service.IExportListener;

interface IExportService {  
	ExportStatus[] getExportStatus(in String packageName);
	
	void exportResource(in String packageName, in int resourceId);
	 
	void addExportListener(IExportListener exportListener);
	 
	void removeExportListener(IExportListener exportListener);
}