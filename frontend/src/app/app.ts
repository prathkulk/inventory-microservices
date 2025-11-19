import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { InventoryService } from './services/inventory.service';
import { OrderService, OrderRequest } from './services/order.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatInputModule,
    MatCardModule,
    MatIconModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private inventoryService = inject(InventoryService);
  private orderService = inject(OrderService);

  skuCode: string = 'IPHONE_17_PRO';
  stockStatus: string = 'unknown';
  quantity: number | null = null;

  checkInventory() {
    this.stockStatus = 'Checking...';

    // 1. Check if it exists
    this.inventoryService.checkStock(this.skuCode).subscribe({
      next: (isInStock) => {
        this.stockStatus = isInStock ? 'In Stock ✅' : 'Out of Stock ❌';

        // 2. If in stock, get the quantity
        if (isInStock) {
          this.inventoryService.getStock(this.skuCode).subscribe((qty) => {
            this.quantity = qty;
          });
        } else {
          this.quantity = 0;
        }
      },
      error: (err) => {
        this.stockStatus = 'Error connecting to server ⚠️';
        console.error(err);
      },
    });
  }

  orderStatus: string = '';

  buyItem() {
    if (!this.quantity || this.quantity <= 0) return;

    this.orderStatus = 'Placing Order...';

    const order: OrderRequest = {
      orderLineItemsDtoList: [
        {
          skuCode: this.skuCode,
          price: 100, // Hardcoded price for demo
          quantity: 1,
        },
      ],
    };

    this.orderService.placeOrder(order).subscribe({
      next: (response) => {
        this.orderStatus = 'Success: ' + response;
        // Refresh stock after buying
        this.checkInventory();
      },
      error: (err) => {
        this.orderStatus = 'Failed to place order.';
        console.error(err);
      },
    });
  }
}
