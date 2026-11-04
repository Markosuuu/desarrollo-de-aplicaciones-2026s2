package com.prontaentrega.controllers.dtos.subastas;

import java.util.List;

public record SubastaPagedResponse(
        List<SubastaResponse> subastas,
        int page,
        int size
) {
}
