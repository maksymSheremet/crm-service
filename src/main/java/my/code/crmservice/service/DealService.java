package my.code.crmservice.service;

import my.code.crmservice.dto.request.CloseDealRequest;
import my.code.crmservice.dto.request.CreateDealRequest;
import my.code.crmservice.dto.request.UpdateDealRequest;
import my.code.crmservice.dto.response.DealResponse;

import java.util.List;
import java.util.UUID;

public interface DealService {

    List<DealResponse> getDeals(UUID clientId, Long userId);

    DealResponse createDeal(UUID clientId, CreateDealRequest request, Long userId);

    DealResponse updateDeal(UUID clientId, UUID dealId, UpdateDealRequest request, Long userId);

    // Окремий метод для lifecycle операції: close тригерить Kafka event
    DealResponse closeDeal(UUID clientId, UUID dealId, CloseDealRequest request, Long userId);
}
