
package br.com.snacksys.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    private Long id;

    @Column(name = "controla_estoque", nullable = false)
    private boolean controlaEstoque;

    protected Produto() {
    }

    public Long getId() {
        return id;
    }

    public boolean isControlaEstoque() {
        return controlaEstoque;
    }
}
