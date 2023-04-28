package br.com.alurafood.pagamentos.controller;

import java.net.URI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alurafood.pagamentos.service.PagamentoService;
import dto.PagamentoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pagamentos")
public class PagamentoController {

	private final PagamentoService service;
	
	private final RabbitTemplate rabbitTemplate;


	@GetMapping
	public Page<PagamentoDto> listar(@PageableDefault(size = 10) Pageable paginacao) {
		return service.obterTodos(paginacao);
	}


	@GetMapping("/{id}")
	public ResponseEntity<PagamentoDto> detalhar(@PathVariable @NotNull Long id) {
		PagamentoDto dto = service.obterPorId(id);

		return ResponseEntity.ok(dto);
	}

	@PostMapping
	public ResponseEntity<PagamentoDto> cadastrar(@RequestBody @Valid PagamentoDto dto, UriComponentsBuilder uriBuilder) {
		PagamentoDto pagamento = service.criarPagamento(dto);
		URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();
		
		
		rabbitTemplate.convertAndSend("pagamentos.ex","", pagamento);
		return ResponseEntity.created(endereco).body(pagamento);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PagamentoDto> atualizar(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDto dto) {
		PagamentoDto atualizado = service.atualizarPagamento(id, dto);
		return ResponseEntity.ok(atualizado);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<PagamentoDto> remover(@PathVariable @NotNull Long id) {
		service.excluirPagamento(id);
		return ResponseEntity.noContent().build();
	}


	@PatchMapping("/{id}/confirmar")
	@CircuitBreaker(name = "atualizaPedido", fallbackMethod = "")
	public void confirmarPagamento(@PathVariable @NotNull Long id){
		service.confirmarPagamento(id);
	}
}
