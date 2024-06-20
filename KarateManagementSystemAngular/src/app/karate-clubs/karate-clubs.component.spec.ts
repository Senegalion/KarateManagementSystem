import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KarateClubsComponent } from './karate-clubs.component';

describe('KarateClubsComponent', () => {
  let component: KarateClubsComponent;
  let fixture: ComponentFixture<KarateClubsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KarateClubsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(KarateClubsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
