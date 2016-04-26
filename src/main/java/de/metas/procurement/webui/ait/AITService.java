package de.metas.procurement.webui.ait;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import de.metas.procurement.sync.IAgentSync;
import de.metas.procurement.sync.protocol.SyncBPartnersRequest;
import de.metas.procurement.sync.protocol.SyncProductSupply;
import de.metas.procurement.sync.protocol.SyncProductsRequest;
import de.metas.procurement.sync.protocol.SyncWeeklySupply;
import de.metas.procurement.webui.model.BPartner;
import de.metas.procurement.webui.model.ContractLine;
import de.metas.procurement.webui.model.Product;
import de.metas.procurement.webui.model.Trend;
import de.metas.procurement.webui.repository.BPartnerRepository;
import de.metas.procurement.webui.repository.ContractLineRepository;
import de.metas.procurement.webui.repository.ProductRepository;
import de.metas.procurement.webui.service.IProductSuppliesService;
import de.metas.procurement.webui.util.DateRange;

/*
 * #%L
 * metasfresh-procurement-webui
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 *
 * @author metas-dev <dev@metasfresh.com>
 * @task https://metasfresh.atlassian.net/browse/FRESH-243
 */
@Path("/aitService/")
@Service
public class AITService
{
	@Autowired(required = true)
	@Lazy
	private IProductSuppliesService poductSuppliesService;

	@Autowired(required = true)
	@Lazy
	private IAgentSync agentSync;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	@Lazy
	private BPartnerRepository bpartnersRepository;

	@Autowired
	@Lazy
	private ContractLineRepository contractLineRepository;

	@POST
	@Path("reportProductSupply")
	@Consumes(MediaType.APPLICATION_JSON)
	public void reportProductSupply(final SyncProductSupply syncProductSupply)
	{
		final BPartner bpartner = bpartnersRepository.findByUuid(syncProductSupply.getBpartner_uuid());
		final Product product = productRepository.findByUuid(syncProductSupply.getUuid());
		final ContractLine contractLine = contractLineRepository.findByUuid(syncProductSupply.getContractLine_uuid());

		poductSuppliesService.reportSupply(bpartner,
				product,
				contractLine,
				syncProductSupply.getDay(),
				syncProductSupply.getQty());
	}

	@POST
	@Path("reportWeeklySupply")
	@Consumes(MediaType.APPLICATION_JSON)
	public void reportWeeklySupply(final SyncWeeklySupply syncWeeklySupply)
	{
		final BPartner bpartner = bpartnersRepository.findByUuid(syncWeeklySupply.getBpartner_uuid());
		final Product product = productRepository.findByUuid(syncWeeklySupply.getUuid());
		final DateRange week = DateRange.createWeek(syncWeeklySupply.getWeekDay());
		final Trend trend = Trend.ofCodeOrNull(syncWeeklySupply.getTrend());

		poductSuppliesService.setNextWeekTrend(bpartner, product, week, trend);
	}

	@POST
	@Path("importBPartners")
	@Consumes(MediaType.APPLICATION_JSON)
	public void importBPartners(final SyncBPartnersRequest request)
	{
		agentSync.syncBPartners(request);
	}

	@POST
	@Path("importProducts")
	@Consumes(MediaType.APPLICATION_JSON)
	public void importProducts(final SyncProductsRequest request)
	{
		agentSync.syncProducts(request);
	}
}
