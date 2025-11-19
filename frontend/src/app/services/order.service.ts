import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OrderRequest {
  orderLineItemsDtoList: Array<{
    skuCode: string;
    price: number;
    quantity: number;
  }>;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  // Proxy forwards /api to port 9090
  private apiUrl = '/api/order'; 

  placeOrder(orderRequest: OrderRequest): Observable<string> {
    // We expect a plain text response ("Order Placed Successfully"), 
    // so we must tell Angular not to parse it as JSON.
    return this.http.post(this.apiUrl, orderRequest, { responseType: 'text' });
  }
}