/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.dataProcessors.dataSources.synthetic;

import jama.Matrix;
import jama.MatrixIO;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.MassSpecOutputDataModel;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_OP_PhoenixTypeA;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;


class SyntheticDataSourceProcessorOPPhoenixTypeATest {

    private static final ResourceExtractor RESOURCE_EXTRACTOR
            = new ResourceExtractor(Tripoli.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void prepareInputDataModelFromFile() throws IOException {
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        DataSourceProcessor_OP_PhoenixTypeA syntheticDataSourceProcessorOPPhoenixTypeA = new DataSourceProcessor_OP_PhoenixTypeA();
        MassSpecOutputDataModel massSpecOutputDataModel = syntheticDataSourceProcessorOPPhoenixTypeA.prepareInputDataModelFromFile(dataFile);

        double[] testArray = new double[]{1, 2, 3, 4, 5};
        Matrix test = new Matrix(testArray, testArray.length);
        MatrixIO.print(2, 2, test);
    }
}