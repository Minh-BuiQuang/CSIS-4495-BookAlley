using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace BookAlleyWebApi.Migrations
{
    /// <inheritdoc />
    public partial class mssqlazure_migration_672 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "IsDeleted",
                table: "Posts",
                type: "bit",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "IsDeleted",
                table: "Posts");
        }
    }
}
