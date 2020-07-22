package br.com.alura.microservice.loja.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.alura.microservice.loja.client.FornecedorClient;
import br.com.alura.microservice.loja.client.TransportadorClient;
import br.com.alura.microservice.loja.controller.dto.CompraDTO;
import br.com.alura.microservice.loja.controller.dto.InfoEntregaDTO;
import br.com.alura.microservice.loja.controller.dto.InfoFornecedorDTO;
import br.com.alura.microservice.loja.controller.dto.InfoPedidoDTO;
import br.com.alura.microservice.loja.controller.dto.VoucherDTO;
import br.com.alura.microservice.loja.model.Compra;
import br.com.alura.microservice.loja.model.StatusCompra;
import br.com.alura.microservice.loja.repository.CompraRepository;

@Service
public class CompraService {
	/*
	 * @Autowired private RestTemplate client;
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CompraService.class);
	
	@Autowired
	private FornecedorClient fornecedorClient;
	
	@Autowired
	private TransportadorClient transportadorClient;
	
	
	
	@Autowired
	private CompraRepository compraRepository;
	
	@Autowired
	private DiscoveryClient eurekaClient;
	
	@HystrixCommand(threadPoolKey ="getByIdThreadPool") 
	public Compra getById(Long id) {
		return compraRepository.findById(id).orElse(null);
	}
	
	@HystrixCommand(fallbackMethod = "realizaCompraFallback",threadPoolKey ="realizaCompraThreadPool" )
	public Compra realizaCompra(@RequestBody CompraDTO compra) {
		Compra compraSalva = new Compra();
		compraSalva.setEnderecoDestino(compra.getEndereco().toString());
		compraSalva.setStatus(StatusCompra.RECEBIDO);
		compraRepository.save(compraSalva);
		compra.setComprId(compraSalva.getId());
		
		LOG.info("Buscando informações do fornecedor de {}",compra.getEndereco().getEstado());
		InfoFornecedorDTO fornecedor = fornecedorClient.getInfoPorEstado(compra.getEndereco().getEstado());
		
		LOG.info("Realizando um pedido");
		InfoPedidoDTO infoPedido = fornecedorClient.realizaPedido(compra.getItens());
		
		compraSalva.setPedidoId(infoPedido.getId());
		compraSalva.setTempoDePreparo(infoPedido.getTempoDePreparo());		
		compraSalva.setStatus(StatusCompra.PEDIDO_REALIZADO);		
		compraRepository.save(compraSalva);
		
		InfoEntregaDTO infoEntregaDTO = new InfoEntregaDTO();
		infoEntregaDTO.setPedidoId(infoPedido.getId());
		infoEntregaDTO.setDataParaEntrega(LocalDate.now().plusDays(infoPedido.getTempoDePreparo()));
		infoEntregaDTO.setEnderecoDestino(compra.getEndereco().toString());
		infoEntregaDTO.setEnderecoOrigem(fornecedor.getEndereco());
		
		VoucherDTO voucherDTO = transportadorClient.reservaEntrega(infoEntregaDTO);		
		compraSalva.setStatus(StatusCompra.RESERVA_ENTREGA_REALIZADA);	
		compraSalva.setVoucher(voucherDTO.getNumero());
		compraSalva.setPrevisaoParaEntrega(voucherDTO.getPrevisaoParaEntrega());
		compraRepository.save(compraSalva);
		/*System.out.println("#############################################################");
		System.out.println(fornecedor.getEndereco());
		System.out.println("#############################################################");
		listClient();*/
		
		return compraSalva;
	}
	
	public Compra realizaCompraFallback(@RequestBody CompraDTO compra) {
		Compra compraSalva = null;
		if(compra.getComprId() != null) {
			compraSalva = compraRepository.findById(compra.getComprId()).orElse(null);
		}else {
			compraSalva = new Compra();
			compraSalva.setEnderecoDestino("Errooooooo");
		}
		
		
		return compraSalva;
	}
	/*
	/*
	 * public void realizaCompra(@RequestBody CompraDTO compra) {
	 * 
	 * ResponseEntity<InfoFornecedorDTO> exchange =
	 * client.exchange("http://fornecedor/info/"+compra.getEndereco().getEstado(),
	 * HttpMethod.GET, null, InfoFornecedorDTO.class);
	 * 
	 * System.out.println(exchange.getBody().getEndereco()); listClient(); }
	 */
	
	public void listClient() {
		eurekaClient.getInstances("fornecedor").stream().
		forEach(fornecedor -> {
			System.out.println("Host:"+fornecedor.getHost()+" -Port:"+fornecedor.getPort());
		});
	}

	

}
