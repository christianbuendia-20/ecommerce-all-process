package com.allprocess.ecommerce.controllers;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller; // <-- Cambio importante
import org.springframework.ui.Model; // <-- Para pasar datos al HTML
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller // Le dice a Spring que devolveremos "Vistas" (Páginas HTML), no JSON
@RequestMapping("/productos") // La URL será http://localhost:8080/productos
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // Para ver toda la vitrina de All Process
    @GetMapping
    public String listarCatalogo(Model model) {

        List<ProductoCatalogoDTO> catalogo = productoService.listarCatalogo();

        // "Empaquetamos" la lista y se la mandamos al HTML con el nombre "productos"
        model.addAttribute("productos", catalogo);

        // Esto le dice a Spring que busque un archivo llamado "catalogo.html"
        return "catalogo";
    }

    // Para ver el detalle de una laptop o componente específico
    @GetMapping("/{idProducto}")
    public String obtenerProductoPorId(@PathVariable Integer idProducto, Model model) {

        ProductoCatalogoDTO producto = productoService.obtenerProductoPorId(idProducto);

        // "Empaquetamos" ese producto específico y se lo mandamos al HTML
        model.addAttribute("producto", producto);

        // Busca el archivo "detalle-producto.html"
        return "detalle-producto";
    }
}