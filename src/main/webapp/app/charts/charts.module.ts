import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

import { SmarthealthcareSharedModule } from '../shared';

import { chartsStates } from './charts.route';

import { EchartsDirective } from './directive/charts.directive';
import { BigmapComponent } from './bigmap/bigmap.component';
import { TopTenBarChartComponent } from './bigmap/toptenbarcharts/toptenbarchart.component';

@NgModule({
    imports: [
        CommonModule,
        SmarthealthcareSharedModule,
        HttpClientModule,
        RouterModule.forRoot(chartsStates, {useHash: true})
    ],
    exports: [],
    declarations: [
        EchartsDirective,
        TopTenBarChartComponent,
        BigmapComponent
    ],
    providers: []
})
export class SmarthealthcareChartsModule {}
