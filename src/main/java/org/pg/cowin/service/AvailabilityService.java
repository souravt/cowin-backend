package org.pg.cowin.service;

import java.util.Collection;

import org.pg.cowin.controller.AvailabilityRequest;
import org.pg.cowin.controller.AvailabilityResponse;
import org.pg.cowin.controller.SlotAdditionRequest;
import org.pg.cowin.controller.SlotAdditionResponse;
import org.pg.cowin.controller.StatusEnum;
import org.pg.cowin.dao.AvailabilityRepository;
import org.pg.cowin.dao.LocationRepository;
import org.pg.cowin.dao.ScheduledSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

	@Autowired
	AvailabilityRepository availabillty;

	@Autowired
	LocationRepository locationRepo;

	public AvailabilityResponse findAvailability(AvailabilityRequest request) {

		AvailabilityResponse response = new AvailabilityResponse();

		Collection<ScheduledSlot> slots = availabillty.findAvailability(request.getVaxCenterId(), request.getVaxDate(),
				request.getRequestReceivedAt());

		response.setVaxCenterId(request.getVaxCenterId());
		response.setVaxDate(request.getVaxDate());
		response.setSlots(slots);

		return response;
	}

	public SlotAdditionResponse addSlots(SlotAdditionRequest request) {
		SlotAdditionResponse response = new SlotAdditionResponse();
		if (locationRepo.findById(request.getVaxCenterId()) == null) {
			response.setStatus(StatusEnum.FAIL);
		} else {
			availabillty.addSlots(request.getVaxCenterId(), request.getVaxDate(), request.getSlotsAdded());
			response.setStatus(StatusEnum.SUCCESS);
		}
		return response;
	}

}
