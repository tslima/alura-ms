package br.com.alurafood.pagamentos.service;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.alurafood.pagamentos.http.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;
import dto.PagamentoDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {
	
	private final PagamentoRepository repository;
	private final ModelMapper modelMapper;
	private final PedidoClient pedido;
	
	 public Page<PagamentoDto> obterTodos(Pageable paginacao) {
	        return repository
	                .findAll(paginacao)
	                .map(p -> modelMapper.map(p, PagamentoDto.class));
	        }
	 
	 public PagamentoDto obterPorId(Long id) {
	        Pagamento pagamento = repository.findById(id)
	                .orElseThrow(() -> new EntityNotFoundException());

	        return modelMapper.map(pagamento, PagamentoDto.class);
	    }
	 
	 public PagamentoDto criarPagamento(PagamentoDto dto) {
	        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
	        pagamento.setStatus(Status.CRIADO);
	        repository.save(pagamento);

	        return modelMapper.map(pagamento, PagamentoDto.class);
	    }
	 
	  public PagamentoDto atualizarPagamento(Long id, PagamentoDto dto) {
	        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
	        pagamento.setId(id);
	        pagamento = repository.save(pagamento);
	        return modelMapper.map(pagamento, PagamentoDto.class);
	    }
	  
	  public void excluirPagamento(Long id) {
	        repository.deleteById(id);
	    }
	  
	   public void confirmarPagamento(Long id){
	        Optional<Pagamento> pagamento = repository.findById(id);

	        if (!pagamento.isPresent()) {
	            throw new EntityNotFoundException();
	        }

	        pagamento.get().setStatus(Status.CONFIRMADO);
	        repository.save(pagamento.get());
	        pedido.atualizaPagamento(pagamento.get().getPedidoId());
	    }

	public void alteraStatus(Long id) {
		Optional<Pagamento> pagamento = repository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        repository.save(pagamento.get());
		
	}
}
