import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";

export interface InventoryItem {
    id?: number;
    skuCode: string;
    quantity: number;   
}

@Injectable({
    providedIn: 'root'
})
export class InventoryService {
    private http = inject(HttpClient);

    private apiUrl = '/api/inventory';

    checkStock(skuCode: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/check?skuCode=${skuCode}`);
    }

    getStock(skuCode: string): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/${skuCode}`);
    }
}