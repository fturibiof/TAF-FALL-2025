import { Component, Input, OnInit } from '@angular/core';
import { BoardAdminService } from '../board-admin.service';

@Component({
  selector: 'app-feature-health-cards',
  templateUrl: './feature-health-cards.component.html',
  styleUrls: ['./feature-health-cards.component.less']
})
export class FeatureHealthCardsComponent implements OnInit {
  @Input() project = 'TAF';
  @Input() days = 30;
  @Input() top = 5;

  cards: any[] = [];
  loading = false;

  constructor(private boardAdminService: BoardAdminService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.boardAdminService.getStatsByRequirement(this.project, this.days).subscribe({
      next: rows => {
        this.cards = (rows || []).slice(0, this.top);
        this.loading = false;
      },
      error: _ => {
        this.cards = [];
        this.loading = false;
      }
    });
  }

  passRate(r: any): string {
    const passed = Number(r?.passed ?? 0);
    const failed = Number(r?.failed ?? 0);
    const total = passed + failed;
    if (!total) return '0%';
    return `${Math.round((passed / total) * 100)}%`;
  }
}