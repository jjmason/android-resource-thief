package jm.rt.service;
import jm.rt.service.ExportStatus;
interface IStatusListener {
	void onStatusChanged(in ExportStatus status);
}