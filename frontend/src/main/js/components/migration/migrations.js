import React, {Component, useEffect, useMemo, useState} from "react";

import useMigrations from "../../hooks/useMigrations";

import {useTable} from 'react-table';
import {QueryClient, QueryClientProvider} from "react-query";


const TableQuery = () => {

    const [tableData, setTableData] = useState(null);
    const {isLoading, isError, data, error} = useMigrations();


    useEffect(() => {
        setTableData(data);
    }, [data])

    if (isLoading || !tableData) {
        return <div>Loading...</div>
    }
    console.log(data);

    return (
        <MigrationsTable tableData={tableData}/>
    );
}

const TableLayout = ({
                         getTableProps,
                         getTableBodyProps,
                         headerGroups,
                         rows,
                         prepareRow,
                     }) => {
    return (
        <table {...getTableProps()}>
            <thead>
            {headerGroups.map(headerGroup => (
                <tr {...headerGroup.getHeaderGroupProps()}>
                    {headerGroup.headers.map(column => (
                        <th {...column.getHeaderProps()}>{column.render('Header')}</th>
                    ))}
                </tr>
            ))}
            </thead>
            <tbody {...getTableBodyProps()}>
            {rows.map((row, i) => {
                prepareRow(row)
                return (
                    <tr {...row.getRowProps()}>
                        {row.cells.map(cell => {
                            return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>
                        })}
                    </tr>
                )
            })}
            </tbody>
        </table>
    );
}


const MigrationsTable = ({tableData}) => {
    const [columns, data] = useMemo(
        () => {
            const columns = [
                {
                    Header: 'Name',
                    accessor: 'shortName'
                },
                {
                    Header: 'Description',
                    accessor: 'description'
                },
                {
                    Header: 'State',
                    accessor: 'state'
                }
            ];
            return [columns, tableData];
        },
        [tableData]
    );

    const tableInstance = useTable({columns, data});

    return (
        <TableLayout {...tableInstance} />
    );
}


const queryClient = new QueryClient()

const MigrationList = () => {
    return (
        <QueryClientProvider client={queryClient}>
            <TableQuery/>
        </QueryClientProvider>
    )

}

export default MigrationList;
